package com.example.cuan.data.repository

import com.example.cuan.data.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Interface representing transaction data operations
 */
interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
}
