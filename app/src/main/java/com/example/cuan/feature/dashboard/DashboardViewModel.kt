package com.example.cuan.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionType
import com.example.cuan.data.repository.TransactionRepository
import com.example.cuan.feature.dashboard.components.CategoryChartData
import com.example.cuan.feature.dashboard.components.CategoryColors
import com.example.cuan.feature.dashboard.components.DailyChartData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val isProfileComplete: Boolean = false,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val recentTransactions: List<Transaction> = emptyList(),
    val weeklyData: List<DailyChartData> = emptyList(),
    val categoryData: List<CategoryChartData> = emptyList(),
    val anomalyMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load user data
            appDataStore.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        viewModelScope.launch {
            appDataStore.isProfileComplete.collect { complete ->
                _uiState.update { it.copy(isProfileComplete = complete) }
            }
        }

        // Collect transactions from Room and merge dynamically
        viewModelScope.launch {
            transactionRepository.getAllTransactions().collect { realTransactions ->
                generateTransactionsWithRealData(realTransactions)
                checkForAnomalies()
            }
        }
    }

    private fun generateTransactionsWithRealData(realTransactions: List<Transaction>) {
        val now = DateUtils.nowMillis()
        val today = LocalDate.now()

        val sampleTransactions = listOf(
            Transaction(
                id = "1",
                amount = 5000000,
                type = TransactionType.INCOME,
                category = "Gaji",
                note = "Gaji bulan Mei",
                date = today.minusDays(1),
                timeMillis = now - 86400000,
                source = com.example.cuan.data.model.TransactionSource.MANUAL,
                isSynced = true
            ),
            Transaction(
                id = "2",
                amount = 150000,
                type = TransactionType.EXPENSE,
                category = "Makan",
                note = "Makan siang di mall",
                date = today,
                timeMillis = now,
                source = com.example.cuan.data.model.TransactionSource.MANUAL,
                isSynced = true
            ),
            Transaction(
                id = "3",
                amount = 50000,
                type = TransactionType.EXPENSE,
                category = "Transport",
                note = "Grab ke kantor",
                date = today,
                timeMillis = now - 3600000,
                source = com.example.cuan.data.model.TransactionSource.MANUAL,
                isSynced = true
            ),
            Transaction(
                id = "4",
                amount = 250000,
                type = TransactionType.EXPENSE,
                category = "Belanja",
                note = "Belanja groceries",
                date = today.minusDays(2),
                timeMillis = now - 172800000,
                source = com.example.cuan.data.model.TransactionSource.SCAN,
                isSynced = true
            ),
            Transaction(
                id = "5",
                amount = 75000,
                type = TransactionType.EXPENSE,
                category = "Hiburan",
                note = "Nonton film",
                date = today.minusDays(3),
                timeMillis = now - 259200000,
                source = com.example.cuan.data.model.TransactionSource.MANUAL,
                isSynced = true
            )
        )

        // Merge real database transactions with sample transactions (avoid duplicate IDs)
        val combinedTransactions = (realTransactions + sampleTransactions).distinctBy { it.id }
            .sortedByDescending { it.timeMillis }

        val totalIncome = combinedTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpense = combinedTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // Weekly breakdown: map transactions to Mon-Sun
        val daysOfWeek = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val weeklyIncomeMap = daysOfWeek.associateWith { 0f }.toMutableMap()
        val weeklyExpenseMap = daysOfWeek.associateWith { 0f }.toMutableMap()

        combinedTransactions.forEach { tx ->
            val dayName = when (tx.date.dayOfWeek.value) {
                1 -> "Sen"
                2 -> "Sel"
                3 -> "Rab"
                4 -> "Kam"
                5 -> "Jum"
                6 -> "Sab"
                7 -> "Min"
                else -> "Sen"
            }
            val amountFloat = tx.amount.toFloat()
            if (tx.type == TransactionType.INCOME) {
                weeklyIncomeMap[dayName] = (weeklyIncomeMap[dayName] ?: 0f) + amountFloat
            } else {
                weeklyExpenseMap[dayName] = (weeklyExpenseMap[dayName] ?: 0f) + amountFloat
            }
        }

        val dynamicWeeklyData = daysOfWeek.map { day ->
            DailyChartData(day, weeklyIncomeMap[day] ?: 0f, weeklyExpenseMap[day] ?: 0f)
        }

        // Category breakdown for expenses
        val expenseTransactions = combinedTransactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpenseSum = expenseTransactions.sumOf { it.amount }.toFloat()

        val dynamicCategoryData = if (totalExpenseSum > 0) {
            val categoryGroups = expenseTransactions.groupBy { it.category }
            categoryGroups.entries.mapIndexed { index, entry ->
                val percentage = (entry.value.sumOf { it.amount } / totalExpenseSum) * 100f
                CategoryChartData(
                    label = entry.key,
                    percentage = percentage,
                    color = CategoryColors.getOrElse(index % CategoryColors.size) { CategoryColors[0] }
                )
            }
        } else {
            listOf(
                CategoryChartData("Makan", 45f, CategoryColors[0]),
                CategoryChartData("Transport", 20f, CategoryColors[1]),
                CategoryChartData("Belanja", 15f, CategoryColors[2]),
                CategoryChartData("Kesehatan", 10f, CategoryColors[3])
            )
        }

        _uiState.update { state ->
            state.copy(
                recentTransactions = combinedTransactions.take(8),
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                weeklyData = dynamicWeeklyData,
                categoryData = dynamicCategoryData,
                isLoading = false
            )
        }
    }

    private fun checkForAnomalies() {
        val totalExpense = _uiState.value.totalExpense
        if (totalExpense > 2000000) {
            _uiState.update {
                it.copy(anomalyMessage = "Pengeluaran bulan ini sudah melebihi Rp 2 juta. Hati-hati!")
            }
        } else {
            _uiState.update { it.copy(anomalyMessage = null) }
        }
    }

    fun dismissAnomaly() {
        _uiState.update { it.copy(anomalyMessage = null) }
    }

    fun refreshData() {
        loadData()
    }
}