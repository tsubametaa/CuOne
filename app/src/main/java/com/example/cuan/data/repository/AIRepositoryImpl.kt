package com.example.cuan.data.repository

import com.example.cuan.core.network.ChatMessage
import com.example.cuan.core.network.OpenRouterApiService
import com.example.cuan.core.network.OpenRouterRequest
import com.example.cuan.core.network.OpenRouterResponse
import com.example.cuan.data.model.TransactionType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Serializable
private data class SerializableParsedTransaction(
    val amount: Long = 0L,
    val type: String = "EXPENSE",
    val category: String = "Lainnya",
    val note: String = "",
    val date: String = ""
)

/**
 * Implementation of AIRepository using OpenRouter API with local regex fallback
 */
class AIRepositoryImpl @Inject constructor(
    private val openRouterApiService: OpenRouterApiService
) : AIRepository {

    private val jsonParser = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override suspend fun parseReceipt(ocrText: String, apiKey: String): Result<ParsedTransaction> {
        // Fallback if API key is blank
        if (apiKey.isBlank()) {
            return Result.success(parseReceiptLocalFallback(ocrText))
        }

        return try {
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val prompt = """
                You are a smart financial receipt parser. Analyze the OCR text below and extract:
                1. amount: total nominal of transaction as integer (long, e.g. 50000, do not include cents or dots).
                2. type: 'INCOME' or 'EXPENSE'.
                3. category: one of the allowed categories: 
                   - For EXPENSE: Makan, Transport, Belanja, Hiburan, Kesehatan, Tagihan, Lainnya
                   - For INCOME: Gaji, Freelance, Bisnis, Investasi, Hadiah, Lainnya
                4. note: name of merchant, store, or brief description (e.g. 'Beta Mart').
                5. date: date of transaction as 'yyyy-MM-dd'. If not found in the text, use today's date: $todayStr.

                Output ONLY a JSON object with keys: amount, type, category, note, date. Do not include markdown tags like ```json or any explanation outside of the JSON object.

                OCR TEXT:
                $ocrText
            """.trimIndent()

            val request = OpenRouterRequest(
                messages = listOf(
                    ChatMessage(role = "user", content = prompt)
                )
            )

            val jsonString = jsonParser.encodeToString(OpenRouterRequest.serializer(), request)
            val requestBody = jsonString.toRequestBody("application/json".toMediaType())

            val authorizationHeader = "Bearer $apiKey"
            val responseBody = openRouterApiService.getChatCompletion(
                authorizationHeader = authorizationHeader,
                request = requestBody
            )
            
            val responseString = responseBody.string()
            val openRouterResponse = jsonParser.decodeFromString<OpenRouterResponse>(responseString)
            val assistantContent = openRouterResponse.choices.firstOrNull()?.message?.content?.trim() 
                ?: throw Exception("Empty response from AI")

            // Clean markdown blocks if LLM output contains them
            val cleanedJson = assistantContent
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val serializableResult = jsonParser.decodeFromString<SerializableParsedTransaction>(cleanedJson)
            
            val transactionType = try {
                TransactionType.valueOf(serializableResult.type.uppercase())
            } catch (e: Exception) {
                TransactionType.EXPENSE
            }

            val dateParsed = try {
                LocalDate.parse(serializableResult.date, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now()
            }

            Result.success(
                ParsedTransaction(
                    amount = serializableResult.amount,
                    type = transactionType,
                    category = serializableResult.category,
                    note = serializableResult.note,
                    date = dateParsed
                )
            )
        } catch (e: Exception) {
            // Fallback to local regex parser if anything goes wrong
            try {
                Result.success(parseReceiptLocalFallback(ocrText))
            } catch (fallbackEx: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Local regex rule-based parser in case API key is empty or request fails.
     */
    private fun parseReceiptLocalFallback(ocrText: String): ParsedTransaction {
        var amount = 0L
        var category = "Lainnya"
        var note = "Struk Belanja"
        var date = LocalDate.now()

        // 1. Extract Amount
        // Look for patterns like "Total Rp 95.000", "Rp95.000", "Total: 95.000"
        val totalRegexes = listOf(
            Regex("(?:total|jumlah)[:\\s]*rp\\s*([\\d.,]+)", RegexOption.IGNORE_CASE),
            Regex("rp\\s*([\\d.,]+)", RegexOption.IGNORE_CASE),
            Regex("(?:total|jumlah)[:\\s]*([\\d.,]+)", RegexOption.IGNORE_CASE)
        )

        for (regex in totalRegexes) {
            val match = regex.find(ocrText)
            if (match != null) {
                val rawNumber = match.groupValues[1]
                // Strip dots, commas, spaces to get number
                val cleanNumber = rawNumber.replace(".", "").replace(",", "").trim()
                val parsedVal = cleanNumber.toLongOrNull()
                if (parsedVal != null && parsedVal > 100) { // filter out small digits like 1, 2
                    amount = parsedVal
                    break
                }
            }
        }

        // If amount is still 0, try to find any largest number in the text
        if (amount == 0L) {
            val numbers = Regex("\\b\\d{1,3}(?:\\.\\d{3})+(?!\\d)\\b")
                .findAll(ocrText)
                .map { it.value.replace(".", "").toLongOrNull() ?: 0L }
                .filter { it > 500 }
                .toList()
            if (numbers.isNotEmpty()) {
                amount = numbers.maxOrNull() ?: 0L
            }
        }

        // 2. Extract Category
        val categoryKeywords = mapOf(
            "Makan" to listOf("makan", "food", "resto", "cafe", "kopi", "coffee", "bakery", "warung", "burger", "pizza", "nasi", "mie", "dapur", "martabak"),
            "Transport" to listOf("grab", "gojek", "taxi", "go-car", "grabcar", "bensin", "shell", "pertamina", "toll", "parkir", "mrt", "lrt", "kereta"),
            "Belanja" to listOf("mart", "super", "toko", "indomaret", "alfamart", "transmart", "carefour", "groceries", "uniqlo", "belanja", "h&m", "guardian", "watsons"),
            "Hiburan" to listOf("bioskop", "cinema", "xxi", "cgv", "game", "nonton", "ticket", "karaoke", "timezone"),
            "Kesehatan" to listOf("apotek", "obat", "hospital", "dokter", "klinik", "kimia farma"),
            "Tagihan" to listOf("listrik", "pln", "pdam", "telkom", "wifi", "internet", "bpjs", "pulsa", "indihome")
        )

        val lowerText = ocrText.lowercase()
        for ((cat, keywords) in categoryKeywords) {
            if (keywords.any { it in lowerText }) {
                category = cat
                break
            }
        }

        // 3. Extract Note / Merchant Name
        val lines = ocrText.lines().filter { it.isNotBlank() }
        if (lines.isNotEmpty()) {
            val firstLine = lines.first().trim()
            if (firstLine.length in 3..30) {
                note = firstLine
            }
        }

        // 4. Extract Date
        // Try to match yyyy-mm-dd or dd/mm/yyyy or dd-mm-yyyy
        val dateRegex = Regex("(\\d{2})[-/](\\d{2})[-/](\\d{4})")
        dateRegex.find(ocrText)?.let { match ->
            val day = match.groupValues[1].toIntOrNull() ?: 1
            val month = match.groupValues[2].toIntOrNull() ?: 1
            val year = match.groupValues[3].toIntOrNull() ?: 2026
            date = try {
                LocalDate.of(year, month, day)
            } catch (e: Exception) {
                LocalDate.now()
            }
        }

        return ParsedTransaction(
            amount = amount,
            type = TransactionType.EXPENSE,
            category = category,
            note = note,
            date = date
        )
    }
}
