package com.example.cuan.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.sync.DailyReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.cuan.data.repository.TransactionRepository
import com.example.cuan.core.utils.SummaryImageGenerator
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class SettingsUiState(
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 20
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val transactionRepository: TransactionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            appDataStore.dailyReminderEnabled.collect { enabled ->
                _uiState.update { it.copy(dailyReminderEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            appDataStore.dailyReminderHour.collect { hour ->
                _uiState.update { it.copy(dailyReminderHour = hour) }
            }
        }
    }

    fun setDailyReminder(enabled: Boolean) {
        viewModelScope.launch {
            appDataStore.setDailyReminderEnabled(enabled)
            if (enabled) {
                DailyReminderWorker.schedule(context)
            } else {
                DailyReminderWorker.cancel(context)
            }
        }
    }

    fun setDailyReminderHour(hour: Int) {
        viewModelScope.launch {
            appDataStore.setDailyReminderHour(hour)
        }
    }


    fun shareSummary(context: Context, onShareReady: (android.net.Uri) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAllTransactions().first()
                val uri = SummaryImageGenerator.generateAndGetUri(context, transactions)
                if (uri != null) {
                    onShareReady(uri)
                } else {
                    onError("Gagal membuat gambar ringkasan.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Terjadi kesalahan.")
            }
        }
    }
}