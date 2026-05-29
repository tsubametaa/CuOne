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

            // In a real app, this would:
            // 1. Use ML Kit to extract text from image
            // 2. Send to OpenRouter for parsing
            // For now, simulate with sample OCR text
            
            try {
                // Simulated OCR result - in real app would use ML Kit
                val sampleOcrText = """
                    TOKO BETA MART
                    Jln. Merdeka No. 123
                    
                    Item:
                    Beras 5kg         Rp 65.000
                    minyak Goreng     Rp 18.000
                    Gula Pasir        Rp 12.000
                    
                    Total:           Rp 95.000
                    
                    28/05/2026 14:30
                """.trimIndent()

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        ocrText = sampleOcrText
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