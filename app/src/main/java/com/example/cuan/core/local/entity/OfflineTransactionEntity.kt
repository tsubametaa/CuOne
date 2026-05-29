package com.example.cuan.core.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for offline transaction queue
 */
@Entity(tableName = "transaction_queue")
data class OfflineTransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val note: String,
    val dateMillis: Long,
    val timeMillis: Long,
    val source: String, // "MANUAL", "SCAN", "FREE_TEXT"
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)