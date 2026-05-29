package com.example.cuan.feature.transaction.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.utils.CurrencyUtils
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionSource
import com.example.cuan.data.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanResultUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val date: String = DateUtils.todayFormatted(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val hasUndetectedFields: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ScanResultViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ScanResultUiState())
    val uiState: StateFlow<ScanResultUiState> = _uiState.asStateFlow()

    fun parseOcrText(ocrText: String) {
        // In a real app, this would call OpenRouter API
        // For now, parse simple patterns
        try {
            var amount = ""
            var category = ""
            var note = ""
            
            // Try to extract amount - look for patterns like "Rp XX.XXX" or "Total: Rp XX"
            val amountRegex = Regex("(?:Total|Jumlah)[:\\s]*Rp\\s*([\\d.]+)", RegexOption.IGNORE_CASE)
            amountRegex.find(ocrText)?.let { match ->
                amount = match.groupValues[1].replace(".", "")
            }

            // If no total found, try general number extraction
            if (amount.isEmpty()) {
                val generalAmountRegex = Regex("Rp\\s*([\\d.]+)")
                generalAmountRegex.find(ocrText)?.let { match ->
                    amount = match.groupValues[1].replace(".", "")
                }
            }

            // Try to detect category
            val categoryKeywords = mapOf(
                "Makan" to listOf("makan", "food", "restaurant", "cafe", "kedai"),
                "Transport" to listOf("grab", "gojek", "taxi", "bensin", "parkir"),
                "Belanja" to listOf("mart", "super", "toko", "belanja"),
                "Hiburan" to listOf("bioskop", "movie", "game", "hiburan"),
                "Kesehatan" to listOf("apotek", "rumah sakit", "dokter", "klinik"),
                "Tagihan" to listOf("listrik", "air", "pulsa", "internet")
            )

            val lowerText = ocrText.lowercase()
            for ((cat, keywords) in categoryKeywords) {
                if (keywords.any { it in lowerText }) {
                    category = cat
                    break
                }
            }

            // Extract merchant name (first line usually)
            val lines = ocrText.lines().filter { it.isNotBlank() }
            if (lines.isNotEmpty()) {
                note = lines.first().trim()
            }

            _uiState.update {
                it.copy(
                    amount = amount,
                    category = category,
                    note = note,
                    hasUndetectedFields = amount.isEmpty() || category.isEmpty()
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(hasUndetectedFields = true) }
        }
    }

    fun updateAmount(amount: String) {
        val filtered = amount.filter { it.isDigit() }
        _uiState.update { it.copy(amount = filtered) }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { it.copy(type = type, category = "") }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveTransaction() {
        val state = _uiState.value
        
        if (state.amount.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Nominal tidak boleh kosong") }
            return
        }

        if (state.category.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Pilih kategori") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // In real app: save to Room, sync to Sheets
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal menyimpan: ${e.message}"
                    )
                }
            }
        }
    }
}