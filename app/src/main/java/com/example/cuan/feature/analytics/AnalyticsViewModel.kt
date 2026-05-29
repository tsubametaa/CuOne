package com.example.cuan.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionType
import com.example.cuan.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
class AnalyticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())

    init {
        loadAvailableMonths()
        observeTransactions()
    }

    private fun loadAvailableMonths() {
        val now = YearMonth.now()
        val months = (0..5).map { now.minusMonths(it.toLong()) }
        _uiState.update { it.copy(availableMonths = months, selectedMonth = now) }
    }

    private fun observeTransactions() {
        viewModelScope.launch {
            combine(
                _selectedMonth,
                transactionRepository.getAllTransactions()
            ) { month, transactions ->
                calculateAnalytics(month, transactions)
            }.collect { updatedState ->
                _uiState.update { updatedState }
            }
        }
    }

    private fun calculateAnalytics(month: YearMonth, transactions: List<Transaction>): AnalyticsUiState {
        val daysInMonth = month.lengthOfMonth()
        val today = LocalDate.now()
        val daysPassed = if (month == YearMonth.now()) today.dayOfMonth else daysInMonth

        // Filter transactions for the selected month
        val currentMonthTransactions = transactions.filter { tx ->
            val txMonth = YearMonth.from(tx.date)
            txMonth == month
        }

        val totalIncome = currentMonthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpense = currentMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // Filter transactions for the previous month to calculate comparison
        val prevMonth = month.minusMonths(1)
        val prevMonthTransactions = transactions.filter { tx ->
            val txMonth = YearMonth.from(tx.date)
            txMonth == prevMonth
        }
        val prevMonthExpense = prevMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val comparedToPrevMonth = if (prevMonthExpense > 0) {
            (totalExpense - prevMonthExpense).toFloat() / prevMonthExpense.toFloat()
        } else {
            0f
        }

        // Category breakdown
        val expenseTransactions = currentMonthTransactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpenseSum = expenseTransactions.sumOf { it.amount }.toFloat()

        val categoryBreakdown = if (totalExpenseSum > 0) {
            val categoryGroups = expenseTransactions.groupBy { it.category }
            categoryGroups.map { (category, txList) ->
                val amount = txList.sumOf { it.amount }
                CategoryBreakdownItem(
                    category = category,
                    amount = amount,
                    percentage = amount.toFloat() / totalExpenseSum
                )
            }.sortedByDescending { it.amount }
        } else {
            emptyList()
        }

        val dailyAverage = if (daysPassed > 0) totalExpense / daysPassed else 0L
        val balance = totalIncome - totalExpense

        return AnalyticsUiState(
            selectedMonth = month,
            availableMonths = _uiState.value.availableMonths,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            comparedToPrevMonth = comparedToPrevMonth,
            categoryBreakdown = categoryBreakdown,
            dailyAverage = dailyAverage,
            projectedMonthEnd = balance,
            daysPassed = daysPassed,
            totalDays = daysInMonth,
            topExpenses = categoryBreakdown.take(5),
            isLoading = false
        )
    }

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }
}