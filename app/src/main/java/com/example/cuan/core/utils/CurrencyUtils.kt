package com.example.cuan.core.utils

import java.text.NumberFormat
import java.util.Locale

// Currency utility functions for Indonesian Rupiah

object CurrencyUtils {
    private val indonesianLocale = Locale("id", "ID")
    private val currencyFormatter = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
        maximumFractionDigits = 0
    }

    // Format amount to Indonesian Rupiah string
    // Example: 1500000 -> "Rp 1.500.000"
    
    fun formatRupiah(amount: Long): String {
        return "Rp ${formatNumber(amount)}"
    }

    // Format amount without currency prefix
    // Example: 1500000 -> "1.500.000"
    
    fun formatNumber(amount: Long): String {
        return NumberFormat.getNumberInstance(indonesianLocale).format(amount)
    }

    // Format with short notation (e.g., 1.5jt, 500rb)
    
    fun formatShort(amount: Long): String {
        return when {
            amount >= 1_000_000_000 -> String.format("%.1fM", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format("%.1fjt", amount / 1_000_000.0)
            amount >= 1_000 -> String.format("%.0frb", amount / 1_000.0)
            else -> amount.toString()
        }
    }

    // Parse Rupiah string to Long
    // Example: "Rp 1.500.000" or "1.500.000" -> 1500000
    
    fun parseRupiah(input: String): Long? {
        return try {
            val cleaned = input
                .replace("Rp", "", ignoreCase = true)
                .replace(".", "")
                .replace(",", ".")
                .trim()
            cleaned.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // Extract numeric value from string
    // Example: "1500000" or "1.500.000" -> 1500000L
    
    fun extractNumber(input: String): Long? {
        return try {
            val cleaned = input
                .replace(Regex("[^0-9]"), "")
            cleaned.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    // Format percentage
    
    fun formatPercentage(value: Float): String {
        return String.format("%.1f%%", value * 100)
    }

    /**
     * Format comparison percentage with indicator
     * Example: 0.15 -> "+15.0%", -0.10 -> "-10.0%"
     */
    fun formatComparison(percentage: Float): String {
        val prefix = if (percentage >= 0) "+" else ""
        return "$prefix${formatPercentage(percentage)}"
    }
}