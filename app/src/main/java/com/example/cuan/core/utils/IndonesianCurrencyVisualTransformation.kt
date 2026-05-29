package com.example.cuan.core.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Visual transformation to format a number input string with Indonesian thousands separators (dot).
 * The input must be digits-only (e.g. "1000000"). The output will display as "1.000.000".
 */
class IndonesianCurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // Clean original text to digits only
        val cleanedText = originalText.filter { it.isDigit() }
        if (cleanedText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        // Format with thousands separator (.)
        val formatted = StringBuilder()
        var digitCount = 0
        for (i in cleanedText.indices.reversed()) {
            formatted.insert(0, cleanedText[i])
            digitCount++
            if (digitCount % 3 == 0 && i > 0) {
                formatted.insert(0, '.')
            }
        }

        val transformedString = formatted.toString()

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(cleanedText.length)
                var originalCount = 0
                var transformedIndex = 0
                while (originalCount < safeOffset && transformedIndex < transformedString.length) {
                    if (transformedString[transformedIndex] != '.') {
                        originalCount++
                    }
                    transformedIndex++
                }
                return transformedIndex
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(transformedString.length)
                var originalCount = 0
                for (i in 0 until safeOffset) {
                    if (transformedString[i] != '.') {
                        originalCount++
                    }
                }
                return originalCount
            }
        }

        return TransformedText(
            AnnotatedString(transformedString),
            offsetMapping
        )
    }
}
