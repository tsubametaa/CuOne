package com.example.cuan.feature.transaction.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

data class AddTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val date: String = DateUtils.todayFormatted(),
    val dateMillis: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val appDataStore: AppDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun updateAmount(amount: String) {
        // Only allow numeric input
        val filtered = amount.filter { it.isDigit() }
        _uiState.update { it.copy(amount = filtered) }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { state ->
            state.copy(
                type = type,
                category = "" // Reset category when type changes
            )
        }
    }

    fun updateCategory(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun updateDate(millis: Long) {
        val date = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        
        _uiState.update { state ->
            state.copy(
                dateMillis = millis,
                date = DateUtils.formatDate(date)
            )
        }
    }

    fun saveTransaction() {
        val state = _uiState.value
        
        if (state.amount.isEmpty() || state.amount == "0") {
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
                // In a real app, this would save to Room and sync to Sheets
                // For now, just mark as saved
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isSaved = true
                    )
                }
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