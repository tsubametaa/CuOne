package com.example.cuan.data.model

import java.time.LocalDate

/**
 * Savings goal model
 */
data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val deadline: LocalDate?,
    val dailySavingsNeeded: Long = 0,
    val weeklySavingsNeeded: Long = 0
) {
    val progress: Float
        get() = if (targetAmount > 0) (currentAmount.toFloat() / targetAmount).coerceIn(0f, 1f) else 0f

    val remainingAmount: Long
        get() = (targetAmount - currentAmount).coerceAtLeast(0)

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount
}