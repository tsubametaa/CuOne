package com.example.cuan.data.repository

import com.example.cuan.core.network.BatchUpdateRequest
import com.example.cuan.core.network.SheetRequest
import com.example.cuan.core.network.AddSheetRequest
import com.example.cuan.core.network.SheetProperties
import com.example.cuan.core.network.SheetsApiService
import com.example.cuan.core.network.SheetsAppendRequest
import com.example.cuan.core.network.SheetsUpdateRequest
import com.example.cuan.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class SheetsRepositoryImpl @Inject constructor(
    private val sheetsApiService: SheetsApiService
) : SheetsRepository {

    private val jsonParser = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun verifySpreadsheet(spreadsheetId: String, accessToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val auth = "Bearer $accessToken"
            sheetsApiService.getSpreadsheet(auth, spreadsheetId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createMonthlyTab(spreadsheetId: String, monthYear: String, accessToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val auth = "Bearer $accessToken"
            
            // Check if tab already exists
            val responseBody = sheetsApiService.getSpreadsheet(auth, spreadsheetId)
            val responseString = responseBody.string()
            val exists = responseString.contains("\"title\": \"$monthYear\"")
            
            if (exists) {
                return@withContext Result.success(true)
            }
            
            // Create the sheet tab
            val addRequest = BatchUpdateRequest(
                requests = listOf(
                    SheetRequest(
                        addSheet = AddSheetRequest(
                            properties = SheetProperties(title = monthYear)
                        )
                    )
                )
            )
            val addJson = jsonParser.encodeToString(BatchUpdateRequest.serializer(), addRequest)
            val addBody = addJson.toRequestBody("application/json".toMediaType())
            sheetsApiService.batchUpdate(auth, spreadsheetId, addBody)
            
            // Append header row
            val headerRequest = SheetsAppendRequest(
                range = "$monthYear!A1",
                values = listOf(
                    listOf("ID", "Tanggal", "Tipe", "Kategori", "Nominal", "Catatan", "Sumber", "Timestamp")
                )
            )
            val headerJson = jsonParser.encodeToString(SheetsAppendRequest.serializer(), headerRequest)
            val headerBody = headerJson.toRequestBody("application/json".toMediaType())
            sheetsApiService.appendValues(auth, spreadsheetId, "$monthYear!A1", request = headerBody)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTransactions(
        spreadsheetId: String,
        transactions: List<Transaction>,
        accessToken: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (transactions.isEmpty()) return@withContext Result.success(true)
        
        try {
            val auth = "Bearer $accessToken"
            
            // Group by formatted month-year (e.g. "Mei 2026")
            val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("id", "ID"))
            val grouped = transactions.groupBy { it.date.format(monthFormatter) }
            
            for ((monthYear, list) in grouped) {
                // Ensure the tab exists
                createMonthlyTab(spreadsheetId, monthYear, accessToken).getOrThrow()
                
                val values = list.map { t ->
                    listOf(
                        t.id,
                        t.date.toString(),
                        t.type.name,
                        t.category,
                        t.amount.toString(),
                        t.note,
                        t.source.name,
                        t.timeMillis.toString()
                    )
                }
                
                val appendRequest = SheetsAppendRequest(
                    range = "$monthYear!A:H",
                    values = values
                )
                val appendJson = jsonParser.encodeToString(SheetsAppendRequest.serializer(), appendRequest)
                val appendBody = appendJson.toRequestBody("application/json".toMediaType())
                
                sheetsApiService.appendValues(
                    authorization = auth,
                    spreadsheetId = spreadsheetId,
                    range = "$monthYear!A:H",
                    request = appendBody
                )
            }
            
            // Update summary tab
            updateSummaryTab(spreadsheetId, accessToken)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSummaryTab(spreadsheetId: String, accessToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val auth = "Bearer $accessToken"
            val summaryTitle = "Ringkasan"
            
            // Check if Ringkasan tab exists
            val responseBody = sheetsApiService.getSpreadsheet(auth, spreadsheetId)
            val responseString = responseBody.string()
            val exists = responseString.contains("\"title\": \"$summaryTitle\"")
            
            if (!exists) {
                val addRequest = BatchUpdateRequest(
                    requests = listOf(
                        SheetRequest(
                            addSheet = AddSheetRequest(
                                properties = SheetProperties(title = summaryTitle)
                            )
                        )
                    )
                )
                val addJson = jsonParser.encodeToString(BatchUpdateRequest.serializer(), addRequest)
                val addBody = addJson.toRequestBody("application/json".toMediaType())
                sheetsApiService.batchUpdate(auth, spreadsheetId, addBody)
            }
            
            // Write summary details
            val todayStr = LocalDate.now().toString()
            val summaryData = listOf(
                listOf("Laporan Keuangan CuOne"),
                listOf("Terakhir Diperbarui:", todayStr),
                emptyList(),
                listOf("Pemberitahuan", "Data sinkronisasi otomatis dari aplikasi CuOne."),
                listOf("Semua rincian transaksi tersimpan di tab bulanan masing-masing.")
            )
            
            val updateRequest = SheetsUpdateRequest(
                range = "$summaryTitle!A1:B5",
                values = summaryData
            )
            val updateJson = jsonParser.encodeToString(SheetsUpdateRequest.serializer(), updateRequest)
            val updateBody = updateJson.toRequestBody("application/json".toMediaType())
            
            sheetsApiService.updateValues(
                authorization = auth,
                spreadsheetId = spreadsheetId,
                range = "$summaryTitle!A1:B5",
                request = updateBody
            )
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
