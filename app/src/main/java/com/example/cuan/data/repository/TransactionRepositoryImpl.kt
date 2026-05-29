package com.example.cuan.data.repository

import com.example.cuan.core.local.dao.TransactionQueueDao
import com.example.cuan.core.local.entity.OfflineTransactionEntity
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionSource
import com.example.cuan.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Implementation of TransactionRepository using Room database
 */
class TransactionRepositoryImpl @Inject constructor(
    private val transactionQueueDao: TransactionQueueDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionQueueDao.getAllTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionQueueDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: String) {
        transactionQueueDao.deleteTransactionById(id)
    }

    // Helper extensions for mapping
    private fun OfflineTransactionEntity.toDomain(): Transaction {
        val localDate = Instant.ofEpochMilli(dateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            
        return Transaction(
            id = id,
            amount = amount,
            type = try { TransactionType.valueOf(type) } catch (e: Exception) { TransactionType.EXPENSE },
            category = category,
            note = note,
            date = localDate,
            timeMillis = timeMillis,
            source = try { TransactionSource.valueOf(source) } catch (e: Exception) { TransactionSource.MANUAL },
            isSynced = isSynced
        )
    }

    private fun Transaction.toEntity(): OfflineTransactionEntity {
        val dateMillis = date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            
        return OfflineTransactionEntity(
            id = id,
            amount = amount,
            type = type.name,
            category = category,
            note = note,
            dateMillis = dateMillis,
            timeMillis = timeMillis,
            source = source.name,
            isSynced = isSynced
        )
    }
}
