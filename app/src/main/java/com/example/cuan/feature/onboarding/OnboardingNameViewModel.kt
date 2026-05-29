package com.example.cuan.feature.onboarding

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

data class OnboardingNameUiState(
    val name: String = "",
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false
)

@HiltViewModel
class OnboardingNameViewModel @Inject constructor(
    private val appDataStore: AppDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingNameUiState())
    val uiState: StateFlow<OnboardingNameUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { state ->
            state.copy(
                name = name,
                nameError = if (name.isNotEmpty() && name.length < 2) "Nama minimal 2 karakter" else null
            )
        }
    }

    fun saveName() {
        val currentName = _uiState.value.name
        if (currentName.length < 2) {
            _uiState.update { it.copy(nameError = "Nama minimal 2 karakter") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Save user name to DataStore
                appDataStore.saveUserName(currentName)
                appDataStore.setOnboardingDone(true)
                _uiState.update { it.copy(isSaved = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(nameError = "Gagal menyimpan", isLoading = false) }
            }
        }
    }
}