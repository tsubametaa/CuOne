package com.example.cuan.data.model

import java.time.LocalDate

/**
 * Transaction model representing income or expense
 */
data class Transaction(
    val id: String,
    val amount: Long,
    val type: TransactionType,
    val category: String,
    val note: String,
    val date: LocalDate,
    val timeMillis: Long,
    val source: TransactionSource,
    val isSynced: Boolean,
    val rawSheetsRowIndex: Int? = null
)

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class TransactionSource {
    MANUAL,
    SCAN,
    FREE_TEXT
}

/**
 * Transaction categories
 */
object TransactionCategories {
    val expenseCategories = listOf(
        "Makan",
        "Transport",
        "Belanja",
        "Kesehatan",
        "Pendidikan",
        "Hiburan",
        "Tagihan",
        "Rumah",
        "Lainnya"
    )

    val incomeCategories = listOf(
        "Gaji",
        "Freelance",
        "Bisnis",
        "Investasi",
        "Hadiah",
        "Lainnya"
    )

    fun getCategoriesForType(type: TransactionType): List<String> {
        return when (type) {
            TransactionType.EXPENSE -> expenseCategories
            TransactionType.INCOME -> incomeCategories
        }
    }
}