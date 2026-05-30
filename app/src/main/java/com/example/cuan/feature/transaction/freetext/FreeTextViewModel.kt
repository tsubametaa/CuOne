package com.example.cuan.feature.transaction.freetext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionSource
import com.example.cuan.data.model.TransactionType
import com.example.cuan.data.repository.AIRepository
import com.example.cuan.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class ParsedTransactionData(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val dateText: String = com.example.cuan.core.utils.DateUtils.todayFormatted(),
    val dateMillis: Long = System.currentTimeMillis(),
    val hour: Int = java.time.LocalTime.now().hour,
    val minute: Int = java.time.LocalTime.now().minute,
    val timeText: String = formatTime(java.time.LocalTime.now().hour, java.time.LocalTime.now().minute)
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

data class FreeTextUiState(
    val inputText: String = "",
    val isProcessing: Boolean = false,
    val parsedData: ParsedTransactionData? = null,
    val showResultSheet: Boolean = false, // True means show the full-screen confirmation view
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class FreeTextViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val transactionRepository: TransactionRepository,
    private val aiRepository: AIRepository,
    private val syncManager: com.example.cuan.core.sync.SyncManager
) : ViewModel() {

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

            try {
                val apiKey = appDataStore.openRouterApiKey.first()
                val parseResult = aiRepository.parseFreeText(input, apiKey)
                
                parseResult.fold(
                    onSuccess = { parsed ->
                        val dateMillis = parsed.date.atStartOfDay(java.time.ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                parsedData = ParsedTransactionData(
                                    amount = if (parsed.amount == 0L) "" else parsed.amount.toString(),
                                    type = parsed.type,
                                    category = parsed.category,
                                    note = parsed.note,
                                    dateText = com.example.cuan.core.utils.DateUtils.formatDate(parsed.date),
                                    dateMillis = dateMillis
                                ),
                                showResultSheet = true
                            )
                        }
                    },
                    onFailure = { e ->
                        throw e
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "AI gagal memproses: ${e.message}. Coba lagi."
                    )
                }
            }
        }
    }

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

    fun updateParsedDate(millis: Long) {
        val date = java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        
        _uiState.update { state ->
            state.copy(
                parsedData = state.parsedData?.copy(
                    dateMillis = millis,
                    dateText = com.example.cuan.core.utils.DateUtils.formatDate(date)
                )
            )
        }
    }

    fun updateParsedTime(hour: Int, minute: Int) {
        _uiState.update { state ->
            state.copy(
                parsedData = state.parsedData?.copy(
                    hour = hour,
                    minute = minute,
                    timeText = ParsedTransactionData.formatTime(hour, minute)
                )
            )
        }
    }

    fun goBackToInput() {
        _uiState.update { it.copy(showResultSheet = false) }
    }

    fun dismissResultSheet() {
        _uiState.update { it.copy(showResultSheet = false) }
    }

    fun saveTransaction() {
        val parsed = _uiState.value.parsedData ?: return

        if (parsed.amount.isEmpty() || parsed.amount == "0") {
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
                val date = java.time.Instant.ofEpochMilli(parsed.dateMillis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()

                val combinedTimeMillis = date.atTime(parsed.hour, parsed.minute)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    amount = parsed.amount.toLongOrNull() ?: 0L,
                    type = parsed.type,
                    category = parsed.category,
                    note = parsed.note,
                    date = date,
                    timeMillis = combinedTimeMillis,
                    source = TransactionSource.FREE_TEXT,
                    isSynced = false
                )
                
                transactionRepository.insertTransaction(transaction)
                viewModelScope.launch {
                    syncManager.syncPendingTransactions()
                }
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Gagal menyimpan: ${e.message}")
                }
            }
        }
    }
}