package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceMasterDao {
    @Query("SELECT * FROM tarang_price_master ORDER BY phase, kw, brand")
    fun getAllPrices(): Flow<List<PriceMasterItem>>

    @Query("SELECT * FROM tarang_price_master WHERE phase = :phase AND kw = :kw AND brand = :brand LIMIT 1")
    suspend fun getPrice(phase: Int, kw: Int, brand: String): PriceMasterItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PriceMasterItem>)

    @Query("SELECT COUNT(*) FROM tarang_price_master")
    suspend fun count(): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerById(id: Int): Flow<Customer?>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerByIdSuspend(id: Int): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)
}

@Dao
interface LedgerTransactionDao {
    @Query("SELECT * FROM ledger_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE customerId = :customerId ORDER BY date DESC")
    fun getTransactionsForCustomer(customerId: Int): Flow<List<LedgerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: LedgerTransaction)
}
