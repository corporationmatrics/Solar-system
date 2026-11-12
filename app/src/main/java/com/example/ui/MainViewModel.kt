package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: Repository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = Repository(db)
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Exposure of global flows
    val allCustomers = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPrices = repository.allPrices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTransactions = repository.allTransactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Stage List Configuration
    val stages = listOf(
        "Lead / Reference Logged",
        "Docs Collected",
        "Sent to Core Team",
        "Physical Submission",
        "Branch Approval",
        "Paycheck from Partner",
        "Installation Deployed",
        "Photos Submitted",
        "Agreement Copy Received",
        "Net Metering Applied",
        "Generation Meter Installed",
        "Subsidy Disbursed",
        "Final Payment Collected"
    )

    // Accounts PIN: no default PIN. The first 4-digit PIN entered becomes the PIN.
    // Only a salted SHA-256 hash is stored (private app prefs, excluded from backups),
    // so it survives app restarts and is never kept in plain text.
    private val pinPrefs = application.getSharedPreferences("tarang_accounts_pin", Context.MODE_PRIVATE)

    private val _isPinCreated = MutableStateFlow(pinPrefs.contains(KEY_PIN_HASH))
    val isPinCreated = _isPinCreated.asStateFlow()

    private var failedPinAttempts = 0
    private var pinLockedUntil = 0L

    private fun hashPin(pin: String, salt: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        val bytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun savePin(pin: String) {
        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        pinPrefs.edit()
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PIN_HASH, hashPin(pin, salt))
            .apply()
        _isPinCreated.value = true
    }

    private fun pinMatches(pin: String): Boolean {
        val salt = pinPrefs.getString(KEY_PIN_SALT, null) ?: return false
        val stored = pinPrefs.getString(KEY_PIN_HASH, null) ?: return false
        val candidate = hashPin(pin, Base64.decode(salt, Base64.NO_WRAP))
        return java.security.MessageDigest.isEqual(
            candidate.toByteArray(Charsets.UTF_8),
            stored.toByteArray(Charsets.UTF_8)
        )
    }

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked = _isUnlocked.asStateFlow()

    private val _activeFilterStage = MutableStateFlow<Int?>(null) // State to pass dashboard tapping filter to Customer tab
    val activeFilterStage = _activeFilterStage.asStateFlow()

    fun setFilterStage(stageNum: Int?) {
        _activeFilterStage.value = stageNum
    }

    fun lockAccounts() {
        _isUnlocked.value = false
    }

    fun verifyPin(enteredPin: String): Boolean {
        if (enteredPin.length != 4 || !enteredPin.all { it.isDigit() }) return false

        // First use: the PIN entered now becomes the accounts PIN.
        if (!_isPinCreated.value) {
            savePin(enteredPin)
            _isUnlocked.value = true
            return true
        }

        val now = System.currentTimeMillis()
        if (now < pinLockedUntil) return false

        if (pinMatches(enteredPin)) {
            failedPinAttempts = 0
            _isUnlocked.value = true
            return true
        }

        failedPinAttempts++
        if (failedPinAttempts >= 5) {
            pinLockedUntil = now + 30_000L // 30 s pause after 5 wrong tries
            failedPinAttempts = 0
        }
        return false
    }

    fun changePin(newPin: String) {
        if (newPin.length == 4 && newPin.all { it.isDigit() }) {
            savePin(newPin)
        }
    }

    // Customer Operations
    fun addNewCustomer(
        name: String,
        mobile: String,
        address: String,
        district: String,
        bpNumber: String,
        portalId: String,
        portalPassword: String,
        referenceName: String,
        systemKw: Int,
        phase: String,
        brand: String,
        sellingPrice: Double,
        associateCommissionAgreed: Double,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val baseCost = repository.getPriceForItem(phase, systemKw, brand)
            val customer = Customer(
                name = name,
                mobile = mobile,
                address = address,
                district = district,
                bpNumber = bpNumber,
                portalId = portalId,
                portalPassword = portalPassword,
                referenceName = referenceName,
                systemKw = systemKw,
                phase = phase,
                brand = brand,
                sellingPrice = sellingPrice,
                tarangBaseCost = baseCost,
                associateCommissionAgreed = associateCommissionAgreed
            )
            repository.insertCustomer(customer)
            onComplete()
        }
    }

    fun updateCustomerStage(customer: Customer, newStage: Int) {
        viewModelScope.launch {
            val updated = customer.copy(
                currentStage = newStage,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateCustomer(updated)
        }
    }

    fun updateCustomerReferenceName(customer: Customer, refName: String) {
        viewModelScope.launch {
            val updated = customer.copy(
                referenceName = refName,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateCustomer(updated)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Expense & Ledger Operations
    fun logTransaction(
        customerId: Int,
        type: String, // "Income" / "Expense"
        category: String, // "Customer Payment", "Fuel", "Commission Payout", "Travel", "Misc"
        amount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val tx = LedgerTransaction(
                customerId = customerId,
                type = type,
                category = category,
                amount = amount,
                notes = notes
            )
            repository.insertTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: LedgerTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Auto backup generation trigger details
    // It creates formatted csv or json content zipped and exposes sharing
    fun getBackupDataAsText(): String {
        val customersData = allCustomers.value
        val transactionsData = allTransactions.value
        val sb = java.lang.StringBuilder()
        sb.append("=== TARANG SOLAR PARTNER PRO BACKUP ===\n")
        sb.append("Generated On: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n\n")
        
        sb.append("--- CUSTOMERS ---\n")
        sb.append("ID,Name,Mobile,BPNumber,KW,Phase,Brand,Stage,SellingPrice,BaseCost\n")
        customersData.forEach { c ->
            sb.append("${c.id},\"${c.name}\",${c.mobile},\"${c.bpNumber}\",${c.systemKw},${c.phase},${c.brand},${c.currentStage},${c.sellingPrice},${c.tarangBaseCost}\n")
        }
        
        sb.append("\n--- TRANSACTIONS ---\n")
        sb.append("ID,CustomerID,Type,Category,Amount,Notes\n")
        transactionsData.forEach { t ->
            sb.append("${t.id},${t.customerId},${t.type},\"${t.category}\",${t.amount},\"${t.notes}\"\n")
        }
        return sb.toString()
    }

    private companion object {
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_PIN_SALT = "pin_salt"
    }
}
