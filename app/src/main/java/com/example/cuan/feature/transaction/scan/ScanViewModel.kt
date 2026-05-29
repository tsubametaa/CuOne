package com.example.cuan.feature.transaction.scan

import android.content.Context
import android.net.Uri
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

data class ScanUiState(
    val selectedImageUri: Uri? = null,
    val isProcessing: Boolean = false,
    val ocrText: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val appDataStore: AppDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun processImage(uri: Uri, context: Context) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun processSelectedImage(context: Context) {
        val uri = _uiState.value.selectedImageUri ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
            
            try {
                val extractedText = OcrProcessor.extractText(context, uri)
                if (extractedText.isBlank()) {
                    throw Exception("Tidak ada teks terdeteksi pada gambar.")
                }
                
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        ocrText = extractedText
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Gagal memproses gambar: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}