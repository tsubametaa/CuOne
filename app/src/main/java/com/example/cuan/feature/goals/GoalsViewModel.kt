package com.example.cuan.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class GoalsUiState(
    val goals: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class GoalsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        // Sample data - in real app would load from Room
        val now = LocalDate.now()
        
        val sampleGoals = listOf(
            SavingsGoal(
                id = "1",
                name = "Liburan ke Bali",
                targetAmount = 5000000,
                currentAmount = 2500000,
                deadline = now.plusMonths(3),
                dailySavingsNeeded = 27778,
                weeklySavingsNeeded = 194444
            ),
            SavingsGoal(
                id = "2",
                name = "Beli Laptop Baru",
                targetAmount = 15000000,
                currentAmount = 5000000,
                deadline = now.plusMonths(6),
                dailySavingsNeeded = 55556,
                weeklySavingsNeeded = 388889
            )
        )

        _uiState.update { it.copy(goals = sampleGoals, isLoading = false) }
    }

    fun addGoal(name: String, targetAmount: Long, currentAmount: Long, deadline: LocalDate?) {
        val dailySavings = if (deadline != null) {
            val remaining = targetAmount - currentAmount
            val days = DateUtils.daysUntil(deadline)
            if (days > 0) remaining / days else 0L
        } else 0L

        val weeklySavings = dailySavings * 7

        val newGoal = SavingsGoal(
            id = UUID.randomUUID().toString(),
            name = name,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            deadline = deadline,
            dailySavingsNeeded = dailySavings,
            weeklySavingsNeeded = weeklySavings
        )

        _uiState.update { state ->
            state.copy(goals = state.goals + newGoal)
        }
    }

    fun deleteGoal(goalId: String) {
        _uiState.update { state ->
            state.copy(goals = state.goals.filter { it.id != goalId })
        }
    }

    fun updateGoalProgress(goalId: String, newAmount: Long) {
        _uiState.update { state ->
            val updatedGoals = state.goals.map { goal ->
                if (goal.id == goalId) {
                    goal.copy(currentAmount = newAmount)
                } else goal
            }
            state.copy(goals = updatedGoals)
        }
    }
}