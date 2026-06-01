package com.example.cuan.data.model

import java.time.YearMonth

// Monthly financial insight data

data class MonthlyInsight(
    val month: YearMonth,
    val totalIncome: Long,
    val totalExpense: Long,
    val netBalance: Long,
    val topCategories: List<CategorySummary>,
    val dailyAverage: Long,
    val projectedMonthEnd: Long,
    val comparedToPrevMonth: Float
)

// Category spending summary
 
data class CategorySummary(
    val category: String,
    val amount: Long,
    val percentage: Float
)

// Anomaly alert for unusual spending
 
data class AnomalyAlert(
    val category: String,
    val currentMonthAmount: Long,
    val averageAmount: Long,
    val multiplier: Float,
    val message: String
)