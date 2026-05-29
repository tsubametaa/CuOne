package com.example.cuan.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.local.dao.TransactionQueueDao
import com.example.cuan.core.local.entity.OfflineTransactionEntity
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionSource
import com.example.cuan.data.model.TransactionType
import com.example.cuan.data.repository.SheetsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDataStore: AppDataStore,
    private val transactionQueueDao: TransactionQueueDao,
    private val sheetsRepository: SheetsRepository
) {
    suspend fun syncPendingTransactions(): Result<Boolean> {
        // 1. Check internet connection
        if (!isNetworkAvailable()) {
            return Result.failure(Exception("Tidak ada koneksi internet. Sinkronisasi ditunda."))
        }

        try {
            // 2. Fetch sheet details from DataStore
            val isConnected = appDataStore.isSheetsConnected.first()
            val spreadsheetId = appDataStore.sheetsId.first()
            val accessToken = appDataStore.googleAccessToken.first()

            if (!isConnected || spreadsheetId.isBlank() || accessToken.isBlank()) {
                return Result.failure(Exception("Google Sheets belum dikonfigurasi atau token kosong."))
            }

            // 3. Query all unsynced transactions from Room
            val unsyncedEntities = transactionQueueDao.getUnsyncedTransactions()
            if (unsyncedEntities.isEmpty()) {
                return Result.success(true)
            }

            val unsyncedTransactions = unsyncedEntities.map { it.toDomain() }

            // 4. Sync to Google Sheets
            val syncResult = sheetsRepository.syncTransactions(
                spreadsheetId = spreadsheetId,
                transactions = unsyncedTransactions,
                accessToken = accessToken
            )

            return syncResult.fold(
                onSuccess = {
                    // 5. Update status in Room to isSynced = true
                    unsyncedEntities.forEach { entity ->
                        transactionQueueDao.updateTransaction(entity.copy(isSynced = true))
                    }
                    // Update last sync time
                    appDataStore.setLastSyncAt(System.currentTimeMillis())
                    Result.success(true)
                },
                onFailure = { e ->
                    Result.failure(e)
                }
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

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
}
