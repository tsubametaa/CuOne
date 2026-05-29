package com.example.cuan.data.repository

import com.example.cuan.data.model.TransactionType
import java.time.LocalDate

/**
 * Represent parsed output from receipt OCR text via AI
 */
data class ParsedTransaction(
    val amount: Long,
    val type: TransactionType,
    val category: String,
    val note: String,
    val date: LocalDate
)

/**
 * Interface for AI text-processing operations
 */
interface AIRepository {
    suspend fun parseReceipt(ocrText: String, apiKey: String): Result<ParsedTransaction>
}
