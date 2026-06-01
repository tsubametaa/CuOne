package com.example.cuan.core.utils

/**
 * Singleton in-memory holder for passing OCR text between screens.
 * Avoids URL-encoding issues when navigating with raw OCR strings.
 */
object OcrTextHolder {
    private var _ocrText: String = ""

    // Store OCR text before navigating to ScanResultScreen
    
    fun set(text: String) {
        _ocrText = text
    }

    // Read and clear the stored OCR text
    
    fun getAndClear(): String {
        val value = _ocrText
        _ocrText = ""
        return value
    }

    // Peek without clearing
    
    fun peek(): String = _ocrText
}
