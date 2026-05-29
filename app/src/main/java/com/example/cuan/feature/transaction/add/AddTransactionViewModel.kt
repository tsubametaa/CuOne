package com.example.cuan.feature.transaction.add

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionSource
import com.example.cuan.data.model.TransactionType
import com.example.cuan.data.repository.AIRepository
import com.example.cuan.data.repository.TransactionRepository
import com.example.cuan.feature.transaction.scan.OcrProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class AddTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val note: String = "",
    val date: String = DateUtils.todayFormatted(),
    val dateMillis: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    
    // AI Receipt features
    val isAnalyzing: Boolean = false,
    val analysisProgressText: String = "",
    val showAutoSaveCountdown: Boolean = false,
    val countdownSeconds: Int = 3,
    val transactionSource: TransactionSource = TransactionSource.MANUAL,

    // Time & Photo States
    val hour: Int = LocalTime.now().hour,
    val minute: Int = LocalTime.now().minute,
    val time: String = AddTransactionViewModel.formatTime(LocalTime.now().hour, LocalTime.now().minute),
    val photoUri: Uri? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val transactionRepository: TransactionRepository,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun updateAmount(amount: String) {
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

    fun updateTime(hour: Int, minute: Int) {
        _uiState.update { state ->
            state.copy(
                hour = hour,
                minute = minute,
                time = formatTime(hour, minute)
            )
        }
    }

    fun updatePhotoUri(uri: Uri?) {
        _uiState.update { state ->
            state.copy(
                photoUri = uri,
                // If user uploads a photo, mark transaction source as SCAN
                transactionSource = if (uri != null) TransactionSource.SCAN else TransactionSource.MANUAL
            )
        }
    }

    fun analyzePhoto(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isAnalyzing = true, 
                    analysisProgressText = "Membaca gambar struk...",
                    errorMessage = null
                )
            }
            
            try {
                // 1. Run ML Kit Text Recognition
                val ocrText = OcrProcessor.extractText(context, uri)
                if (ocrText.isBlank()) {
                    throw Exception("Tidak ada teks yang terdeteksi pada gambar struk.")
                }

                _uiState.update { it.copy(analysisProgressText = "AI sedang menganalisis detail transaksi...") }

                // 2. Load API key and call AI
                val apiKey = appDataStore.openRouterApiKey.first()
                val parseResult = aiRepository.parseReceipt(ocrText, apiKey)
                
                parseResult.fold(
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
                                dateMillis = dateMillis,
                                date = DateUtils.formatDate(parsed.date),
                                transactionSource = TransactionSource.SCAN,
                                isAnalyzing = false,
                                photoUri = uri // Save image reference in state
                            )
                        }

                        // Trigger auto-save countdown
                        startAutoSaveCountdown()
                    },
                    onFailure = { e ->
                        throw e
                    }
                )
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "AI gagal memproses: ${e.message}. Silakan isi manual."
                    )
                }
            }
        }
    }

    private fun startAutoSaveCountdown() {
        countdownJob?.cancel()
        _uiState.update { it.copy(showAutoSaveCountdown = true, countdownSeconds = 3) }
        countdownJob = viewModelScope.launch {
            for (i in 3 downTo 1) {
                _uiState.update { it.copy(countdownSeconds = i) }
                delay(1000)
            }
            saveTransaction()
        }
    }

    fun cancelAutoSave() {
        countdownJob?.cancel()
        _uiState.update { it.copy(showAutoSaveCountdown = false) }
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
            _uiState.update { it.copy(isLoading = true, showAutoSaveCountdown = false) }
            
            try {
                val date = Instant.ofEpochMilli(state.dateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                
                // Combine selected Date and selected Time into single timeMillis
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
                    source = state.transactionSource,
                    isSynced = false
                )
                
                transactionRepository.insertTransaction(transaction)
                
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

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }

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