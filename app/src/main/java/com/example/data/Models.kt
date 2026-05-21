package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "tarang_price_master")
data class PriceMasterItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phase: Int, // 1 for 1Ph, 3 for 3Ph
    val kw: Int, // kW count (e.g., 2, 3, 5, 10, etc.)
    val brand: String, // "Cosmic", "Adani", "TATA"
    val modules: Int, // e.g., 6 Panels
    val inverter: String, // e.g., "3kW Inv"
    val baseCost: Double // Tarang fixed cost
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mobile: String,
    val address: String,
    val district: String,
    val bpNumber: String,
    val portalId: String,
    val portalPassword: String,
    val referenceName: String, // Mihir, Prakash, etc.
    val systemKw: Int, // 2, 3, 5, etc.
    val phase: String, // "1Ph" or "3Ph"
    val brand: String, // "Cosmic", "Adani", "TATA"
    val currentStage: Int = 1, // 1 to 13
    val sellingPrice: Double,
    val tarangBaseCost: Double, // Auto-fetched from PriceMaster
    val associateCommissionAgreed: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ledger_transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int, // Foreign key link (or 0 if global/no specific customer)
    val type: String, // "Income" or "Expense"
    val category: String, // "Customer Payment", "Fuel", "Commission Payout", "Travel", "Misc"
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val notes: String
)
