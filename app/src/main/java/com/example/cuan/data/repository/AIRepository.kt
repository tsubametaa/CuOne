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
    suspend fun parseFreeText(text: String, apiKey: String): Result<ParsedTransaction>
    suspend fun chatWithFinance(
        prompt: String,
        history: List<com.example.cuan.core.network.ChatMessage>,
        apiKey: String
    ): Result<String>
}
