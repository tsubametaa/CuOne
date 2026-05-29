package com.example.cuan.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDatabase
import com.example.cuan.core.local.entity.SavingsGoalEntity
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

data class GoalsUiState(
    val goals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {

    private val savingsGoalDao = database.savingsGoalDao()

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            savingsGoalDao.getAllGoals().collect { entities ->
                val domainGoals = entities.map { entity ->
                    val deadline = entity.deadlineMillis?.let { millis ->
                        Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }

                    val dailySavings = if (deadline != null) {
                        val remaining = entity.targetAmount - entity.currentAmount
                        val days = DateUtils.daysUntil(deadline)
                        if (days > 0 && remaining > 0) remaining / days else 0L
                    } else 0L

                    SavingsGoal(
                        id = entity.id,
                        name = entity.name,
                        targetAmount = entity.targetAmount,
                        currentAmount = entity.currentAmount,
                        deadline = deadline,
                        dailySavingsNeeded = dailySavings,
                        weeklySavingsNeeded = dailySavings * 7
                    )
                }

                _uiState.update { it.copy(goals = domainGoals, isLoading = false) }
            }
        }
    }

    fun addGoal(name: String, targetAmount: Long, currentAmount: Long, deadline: LocalDate?) {
        viewModelScope.launch {
            val deadlineMillis = deadline?.atStartOfDay(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()

            val entity = SavingsGoalEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                deadlineMillis = deadlineMillis
            )

            savingsGoalDao.insertGoal(entity)
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            savingsGoalDao.deleteGoalById(goalId)
        }
    }

    fun updateGoalProgress(goalId: String, newAmount: Long) {
        viewModelScope.launch {
            val existingGoal = savingsGoalDao.getGoalById(goalId) ?: return@launch
            val updated = existingGoal.copy(currentAmount = newAmount)
            savingsGoalDao.updateGoal(updated)
        }
    }
}