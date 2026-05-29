package com.example.cuan.core.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cuan.core.local.entity.OfflineTransactionEntity
import kotlinx.coroutines.flow.Flow

// DAO for transaction queue operations //
@Dao
interface TransactionQueueDao {

    @Query("SELECT * FROM transaction_queue ORDER BY createdAt DESC")
    fun getAllTransactions(): Flow<List<OfflineTransactionEntity>>

    @Query("SELECT * FROM transaction_queue WHERE isSynced = 0 ORDER BY createdAt ASC")
    suspend fun getUnsyncedTransactions(): List<OfflineTransactionEntity>

    @Query("SELECT * FROM transaction_queue WHERE id = :id")
    suspend fun getTransactionById(id: String): OfflineTransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: OfflineTransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: OfflineTransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: OfflineTransactionEntity)

    @Query("DELETE FROM transaction_queue WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transaction_queue")
    suspend fun deleteAllTransactions()

    @Query("SELECT COUNT(*) FROM transaction_queue")
    suspend fun getTransactionCount(): Int
}