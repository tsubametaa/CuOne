package com.example.cuan.core.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity for savings goals
@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long,
    val deadlineMillis: Long?,
    val createdAt: Long = System.currentTimeMillis()
)