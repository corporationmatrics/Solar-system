package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {
    val priceMasterDao = db.priceMasterDao()
    val customerDao = db.customerDao()
    val ledgerTransactionDao = db.ledgerTransactionDao()

    val allPrices: Flow<List<PriceMasterItem>> = priceMasterDao.getAllPrices()
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val allTransactions: Flow<List<LedgerTransaction>> = ledgerTransactionDao.getAllTransactions()

    suspend fun seedDatabaseIfEmpty() {
        val count = priceMasterDao.count()
        if (count == 0) {
            Log.d("Repository", "Seeding Tarang Price Master catalog...")
            val standardList = listOf(
                PriceMasterItem(phase = 1, kw = 2, brand = "Cosmic", modules = 4, inverter = "2kW Inv", baseCost = 120000.0),
                PriceMasterItem(phase = 1, kw = 2, brand = "Adani", modules = 4, inverter = "2kW Inv", baseCost = 135000.0),
                PriceMasterItem(phase = 1, kw = 2, brand = "TATA", modules = 4, inverter = "2kW Inv", baseCost = 150000.0),
                PriceMasterItem(phase = 1, kw = 3, brand = "Cosmic", modules = 6, inverter = "3kW Inv", baseCost = 180000.0),
                PriceMasterItem(phase = 1, kw = 3, brand = "Adani", modules = 6, inverter = "3kW Inv", baseCost = 200000.0),
                PriceMasterItem(phase = 1, kw = 3, brand = "TATA", modules = 6, inverter = "3kW Inv", baseCost = 215000.0),
                PriceMasterItem(phase = 1, kw = 5, brand = "Cosmic", modules = 10, inverter = "5kW Inv", baseCost = 270000.0),
                PriceMasterItem(phase = 1, kw = 5, brand = "Adani", modules = 10, inverter = "5kW Inv", baseCost = 290000.0),
                PriceMasterItem(phase = 1, kw = 5, brand = "TATA", modules = 10, inverter = "5kW Inv", baseCost = 310000.0),
                
                PriceMasterItem(phase = 3, kw = 3, brand = "Cosmic", modules = 6, inverter = "3kW Inv", baseCost = 190000.0),
                PriceMasterItem(phase = 3, kw = 3, brand = "Adani", modules = 6, inverter = "3kW Inv", baseCost = 210000.0),
                PriceMasterItem(phase = 3, kw = 3, brand = "TATA", modules = 6, inverter = "3kW Inv", baseCost = 225000.0),
                PriceMasterItem(phase = 3, kw = 5, brand = "Cosmic", modules = 10, inverter = "5kW Inv", baseCost = 280000.0),
                PriceMasterItem(phase = 3, kw = 5, brand = "Adani", modules = 10, inverter = "5kW Inv", baseCost = 300000.0),
                PriceMasterItem(phase = 3, kw = 5, brand = "TATA", modules = 10, inverter = "5kW Inv", baseCost = 320000.0),
                PriceMasterItem(phase = 3, kw = 10, brand = "Cosmic", modules = 20, inverter = "10kW Inv", baseCost = 480000.0),
                PriceMasterItem(phase = 3, kw = 10, brand = "Adani", modules = 20, inverter = "10kW Inv", baseCost = 520000.0),
                PriceMasterItem(phase = 3, kw = 10, brand = "TATA", modules = 20, inverter = "10kW Inv", baseCost = 550000.0)
            )
            priceMasterDao.insertAll(standardList)
        }
    }

    suspend fun getPriceForItem(phaseStr: String, kw: Int, brand: String): Double {
        val phaseInt = if (phaseStr.contains("3")) 3 else 1
        val item = priceMasterDao.getPrice(phaseInt, kw, brand)
        return item?.baseCost ?: (kw * 65000.0) // Fallback default pricing if mismatch
    }

    fun getCustomerById(id: Int): Flow<Customer?> {
        return customerDao.getCustomerById(id)
    }

    suspend fun getCustomerByIdSuspend(id: Int): Customer? {
        return customerDao.getCustomerByIdSuspend(id)
    }

    suspend fun insertCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    fun getTransactionsForCustomer(customerId: Int): Flow<List<LedgerTransaction>> {
        return ledgerTransactionDao.getTransactionsForCustomer(customerId)
    }

    suspend fun insertTransaction(transaction: LedgerTransaction) {
        ledgerTransactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: LedgerTransaction) {
        ledgerTransactionDao.deleteTransaction(transaction)
    }
}
