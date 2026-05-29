package com.example.cuan.feature.transaction.scan

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Utility to process OCR locally using Google ML Kit Text Recognition
 */
object OcrProcessor {

    /**
     * Extracts text from an image URI asynchronously using Coroutines
     */
    suspend fun extractText(context: Context, uri: Uri): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (!continuation.isCancelled) {
                        continuation.resume(visionText.text)
                    }
                }
                .addOnFailureListener { e ->
                    if (!continuation.isCancelled) {
                        continuation.resumeWithException(e)
                    }
                }
        } catch (e: Exception) {
            if (!continuation.isCancelled) {
                continuation.resumeWithException(e)
            }
        }
    }
}
