package com.example.cuan.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.data.model.IncomeRange
import com.example.cuan.data.model.extractSpreadsheetId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val occupation: String = "",
    val incomeRange: IncomeRange? = null,
    val monthlyBudget: String = "",
    val sheetsUrl: String = "",
    val apiKey: String = "",
    val googleAccessToken: String = "",
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val appDataStore: AppDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val name = appDataStore.userName.first()
            val occupation = appDataStore.userOccupation.first()
            val incomeRangeStr = appDataStore.userIncomeRange.first()
            val monthlyBudget = appDataStore.userMonthlyBudget.first()
            val sheetsUrl = appDataStore.sheetsUrl.first()
            val apiKey = appDataStore.openRouterApiKey.first()
            val googleAccessToken = appDataStore.googleAccessToken.first()

            val incomeRange = try {
                IncomeRange.valueOf(incomeRangeStr)
            } catch (e: Exception) {
                null
            }

            _uiState.update {
                it.copy(
                    name = name,
                    occupation = occupation,
                    incomeRange = incomeRange,
                    monthlyBudget = if (monthlyBudget > 0) monthlyBudget.toString() else "",
                    sheetsUrl = sheetsUrl,
                    apiKey = apiKey,
                    googleAccessToken = googleAccessToken,
                    isLoading = false
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateOccupation(occupation: String) {
        _uiState.update { it.copy(occupation = occupation) }
    }

    fun updateIncomeRange(incomeRange: IncomeRange) {
        _uiState.update { it.copy(incomeRange = incomeRange) }
    }

    fun updateMonthlyBudget(budget: String) {
        val filtered = budget.filter { it.isDigit() }
        _uiState.update { it.copy(monthlyBudget = filtered) }
    }

    fun updateSheetsUrl(url: String) {
        _uiState.update { it.copy(sheetsUrl = url) }
    }

    fun updateApiKey(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey) }
    }

    fun updateGoogleAccessToken(token: String) {
        _uiState.update { it.copy(googleAccessToken = token) }
    }

    fun saveProfile() {
        val state = _uiState.value

        // Validation
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama tidak boleh kosong") }
            return
        }

        if (state.occupation.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pekerjaan tidak boleh kosong") }
            return
        }

        if (state.sheetsUrl.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Link Sheets tidak boleh kosong") }
            return
        }

        val spreadsheetId = extractSpreadsheetId(state.sheetsUrl)
        if (spreadsheetId == null) {
            _uiState.update { it.copy(errorMessage = "Link Sheets tidak valid") }
            return
        }

        if (state.apiKey.isBlank()) {
            _uiState.update { it.copy(errorMessage = "API Key tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Save all fields to DataStore
                appDataStore.saveUserName(state.name)
                appDataStore.saveUserOccupation(state.occupation)
                state.incomeRange?.let { appDataStore.saveUserIncomeRange(it.name) }
                appDataStore.saveUserMonthlyBudget(state.monthlyBudget.toLongOrNull() ?: 0)
                appDataStore.saveSheetsUrl(state.sheetsUrl)
                appDataStore.saveSheetsId(spreadsheetId)
                appDataStore.saveOpenRouterApiKey(state.apiKey)
                appDataStore.saveGoogleAccessToken(state.googleAccessToken)
                
                // Mark profile as complete
                appDataStore.setProfileComplete(true)

                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
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

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}