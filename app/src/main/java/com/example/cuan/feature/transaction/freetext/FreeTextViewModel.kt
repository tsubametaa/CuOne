package com.example.cuan.feature.transaction.freetext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.utils.CurrencyUtils
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParsedTransactionData(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val date: String = DateUtils.todayFormatted()
)

data class FreeTextUiState(
    val inputText: String = "",
    val isProcessing: Boolean = false,
    val parsedData: ParsedTransactionData? = null,
    val showResultSheet: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FreeTextViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FreeTextUiState())
    val uiState: StateFlow<FreeTextUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun processText() {
        val input = _uiState.value.inputText.trim()
        if (input.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }

            // Simulate AI processing delay
            delay(1500)

            try {
                // Simple local parsing (in real app would call OpenRouter)
                val parsed = parseTransactionText(input)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        parsedData = parsed,
                        showResultSheet = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Tidak dapat memahami input, coba lebih spesifik"
                    )
                }
            }
        }
    }

    private fun parseTransactionText(text: String): ParsedTransactionData {
        val lowerText = text.lowercase()
        
        // Detect transaction type based on keywords
        val isIncome = lowerText.contains("terima") || 
                       lowerText.contains("gaji") || 
                       lowerText.contains("bonus") ||
                       lowerText.contains("uang") && lowerText.contains("masuk")
        
        // Extract amount
        val amount = extractAmount(text)
        
        // Detect category
        val category = detectCategory(text, isIncome)
        
        // Generate note from input
        val note = generateNote(text)
        
        return ParsedTransactionData(
            amount = amount,
            type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
            category = category,
            note = note,
            date = DateUtils.todayFormatted()
        )
    }

    private fun extractAmount(text: String): String {
        // Look for patterns like "35rb", "5 juta", "Rp 150.000"
        val patterns = listOf(
            Regex("(\\d+)\\s*jt", RegexOption.IGNORE_CASE),  // e.g., "5jt", "5 jt"
            Regex("(\\d+)\\s*rb", RegexOption.IGNORE_CASE),  // e.g., "35rb", "35 rb"
            Regex("Rp\\s*([\\d.]+)", RegexOption.IGNORE_CASE),  // e.g., "Rp 150.000"
            Regex("([\\d.]+)\\s*ribu", RegexOption.IGNORE_CASE),  // e.g., "35 ribu"
            Regex("([\\d.]+)\\s*juta", RegexOption.IGNORE_CASE)  // e.g., "5 juta"
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                var value = match.groupValues[1].replace(".", "")
                
                // Handle shorthand
                if (text.contains("jt") || text.contains("juta")) {
                    return ((value.toLongOrNull() ?: 0) * 1_000_000).toString()
                } else if (text.contains("rb") || text.contains("ribu")) {
                    return ((value.toLongOrNull() ?: 0) * 1_000).toString()
                }

                return value
            }
        }

        return ""
    }

    private fun detectCategory(text: String, isIncome: Boolean): String {
        val lowerText = text.lowercase()

        if (isIncome) {
            return when {
                lowerText.contains("gaji") -> "Gaji"
                lowerText.contains("freelance") || lowerText.contains("proyek") -> "Freelance"
                lowerText.contains("bisnis") -> "Bisnis"
                lowerText.contains("invest") -> "Investasi"
                lowerText.contains("hadiah") || lowerText.contains("uang") -> "Hadiah"
                else -> "Lainnya"
            }
        }

        return when {
            lowerText.contains("makan") || lowerText.contains("kopi") || 
            lowerText.contains("food") || lowerText.contains("cafe") -> "Makan"
            lowerText.contains("grab") || lowerText.contains("gojek") || 
            lowerText.contains("taxi") || lowerText.contains("transport") -> "Transport"
            lowerText.contains("belanja") || lowerText.contains("mart") -> "Belanja"
            lowerText.contains("movie") || lowerText.contains("bioskop") || 
            lowerText.contains("hiburan") -> "Hiburan"
            lowerText.contains("obat") || lowerText.contains("klinik") || 
            lowerText.contains("dokter") -> "Kesehatan"
            lowerText.contains("listrik") || lowerText.contains("air") || 
            lowerText.contains("pulsa") || lowerText.contains("tagihan") -> "Tagihan"
            else -> "Lainnya"
        }
    }

    private fun generateNote(text: String): String {
        // Clean up the input text to use as note
        val cleaned = text
            .replace(Regex("\\d+\\s*(jt|rb|juta|ribu)", RegexOption.IGNORE_CASE), "")
            .replace(Regex("Rp\\s*[\\d.]+"), "")
            .replace(Regex("\\b(beli|terima|uang|kas)\\b", RegexOption.IGNORE_CASE), "")
            .trim()
        
        return cleaned.take(50) // Limit note length
    }

    // Parsed data updates
    fun updateParsedAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() }
        _uiState.update { state ->
            state.copy(parsedData = state.parsedData?.copy(amount = filtered))
        }
    }

    fun updateParsedType(type: TransactionType) {
        _uiState.update { state ->
            state.copy(parsedData = state.parsedData?.copy(type = type, category = ""))
        }
    }

    fun updateParsedCategory(category: String) {
        _uiState.update { state ->
            state.copy(parsedData = state.parsedData?.copy(category = category))
        }
    }

    fun updateParsedNote(note: String) {
        _uiState.update { state ->
            state.copy(parsedData = state.parsedData?.copy(note = note))
        }
    }

    fun dismissResultSheet() {
        _uiState.update { it.copy(showResultSheet = false) }
    }

    fun saveTransaction() {
        val parsed = _uiState.value.parsedData ?: return

        if (parsed.amount.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Nominal tidak boleh kosong") }
            return
        }

        if (parsed.category.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Pilih kategori") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                // In real app: save to Room, sync to Sheets
                delay(500)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Gagal menyimpan: ${e.message}")
                }
            }
        }
    }
}