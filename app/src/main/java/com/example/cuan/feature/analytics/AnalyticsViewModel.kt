package com.example.cuan.feature.analytics

import androidx.lifecycle.ViewModel
import com.example.cuan.core.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class AnalyticsUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val availableMonths: List<YearMonth> = emptyList(),
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val comparedToPrevMonth: Float = 0f,
    val categoryBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val dailyAverage: Long = 0,
    val projectedMonthEnd: Long = 0,
    val daysPassed: Int = 0,
    val totalDays: Int = 30,
    val topExpenses: List<CategoryBreakdownItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAvailableMonths()
        loadData()
    }

    private fun loadAvailableMonths() {
        val now = YearMonth.now()
        val months = (0..5).map { now.minusMonths(it.toLong()) }
        
        _uiState.update { it.copy(availableMonths = months) }
    }

    private fun loadData() {
        // Sample data - in real app would load from Sheets/Repository
        val now = YearMonth.now()
        val daysInMonth = now.lengthOfMonth()
        val currentDay = LocalDate.now().dayOfMonth

        val sampleCategoryBreakdown = listOf(
            CategoryBreakdownItem("Makan", 850000, 0.34f),
            CategoryBreakdownItem("Transport", 450000, 0.18f),
            CategoryBreakdownItem("Belanja", 600000, 0.24f),
            CategoryBreakdownItem("Hiburan", 250000, 0.10f),
            CategoryBreakdownItem("Tagihan", 350000, 0.14f)
        )

        val totalExpense = sampleCategoryBreakdown.sumOf { it.amount }
        val totalIncome = 5500000L
        val balance = totalIncome - totalExpense

        _uiState.update {
            it.copy(
                selectedMonth = now,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                comparedToPrevMonth = 0.12f, // 12% increase
                categoryBreakdown = sampleCategoryBreakdown,
                dailyAverage = totalExpense / currentDay,
                projectedMonthEnd = balance,
                daysPassed = currentDay,
                totalDays = daysInMonth,
                topExpenses = sampleCategoryBreakdown.sortedByDescending { item -> item.amount }.take(5),
                isLoading = false
            )
        }
    }

    fun selectMonth(month: YearMonth) {
        _uiState.update { it.copy(selectedMonth = month) }
        loadData() // Reload data for selected month
    }
}