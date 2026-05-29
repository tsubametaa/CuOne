package com.example.cuan.feature.transaction.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionSource
import com.example.cuan.data.model.TransactionType
import com.example.cuan.data.repository.AIRepository
import com.example.cuan.data.repository.TransactionRepository
import com.example.cuan.core.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

data class ScanResultUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val date: String = DateUtils.todayFormatted(),
    val dateMillis: Long = System.currentTimeMillis(),
    val hour: Int = LocalTime.now().hour,
    val minute: Int = LocalTime.now().minute,
    val time: String = formatTime(LocalTime.now().hour, LocalTime.now().minute),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val hasUndetectedFields: Boolean = false,
    val errorMessage: String? = null
) {
    companion object {
        fun formatTime(hour: Int, minute: Int): String {
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            return String.format(java.util.Locale.US, "%02d:%02d %s", displayHour, minute, amPm)
        }
    }
}

@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val aiRepository: AIRepository,
    private val transactionRepository: TransactionRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanResultUiState())
    val uiState: StateFlow<ScanResultUiState> = _uiState.asStateFlow()

    private var hasParsed = false

    fun parseOcrText(ocrText: String) {
        if (ocrText.isBlank() || hasParsed) return
        hasParsed = true

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val apiKey = appDataStore.openRouterApiKey.first()
                val result = aiRepository.parseReceipt(ocrText, apiKey)
                
                result.fold(
                    onSuccess = { parsed ->
                        val dateMillis = parsed.date.atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()

                        _uiState.update { state ->
                            state.copy(
                                amount = parsed.amount.toString(),
                                type = parsed.type,
                                category = parsed.category,
                                note = parsed.note,
                                date = DateUtils.formatDate(parsed.date),
                                dateMillis = dateMillis,
                                hasUndetectedFields = parsed.amount == 0L || parsed.category.isEmpty(),
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                hasUndetectedFields = true,
                                errorMessage = "Gagal menganalisis struk: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        hasUndetectedFields = true,
                        errorMessage = "Error: ${e.message}"
                    )
                }
            }
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

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { state ->
            state.copy(
                hour = hour,
                minute = minute,
                time = ScanResultUiState.formatTime(hour, minute)
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
                val date = Instant.ofEpochMilli(state.dateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val combinedTimeMillis = date.atTime(state.hour, state.minute)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = state.amount.toLongOrNull() ?: 0L,
                    type = state.type,
                    category = state.category,
                    note = state.note,
                    date = date,
                    timeMillis = combinedTimeMillis,
                    source = TransactionSource.SCAN,
                    isSynced = false
                )
                
                transactionRepository.insertTransaction(transaction)

                viewModelScope.launch {
                    syncManager.syncPendingTransactions()
                }

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