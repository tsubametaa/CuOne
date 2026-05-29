package com.example.cuan.feature.analytics

/**
 * Data model for category breakdown items in the analytics screen.
 */
data class CategoryBreakdownItem(
    val category: String,
    val amount: Long,
    val percentage: Float
)
