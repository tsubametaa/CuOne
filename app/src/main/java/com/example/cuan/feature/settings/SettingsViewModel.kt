package com.example.cuan.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 20
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDataStore: AppDataStore
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
        }
    }

    fun setDailyReminderHour(hour: Int) {
        viewModelScope.launch {
            appDataStore.setDailyReminderHour(hour)
        }
    }
}