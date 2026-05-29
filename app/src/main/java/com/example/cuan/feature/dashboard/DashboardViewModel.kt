package com.example.cuan.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.core.utils.PdfGenerator
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
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
    val isLoading: Boolean = false,
    val sheetsUrl: String = "",
    val dailyReminderEnabled: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val transactionRepository: TransactionRepository,
    private val syncManager: com.example.cuan.core.sync.SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load user name
            appDataStore.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        viewModelScope.launch {
            appDataStore.isProfileComplete.collect { complete ->
                _uiState.update { it.copy(isProfileComplete = complete) }
            }
        }

        viewModelScope.launch {
            appDataStore.sheetsUrl.collect { url ->
                _uiState.update { it.copy(sheetsUrl = url) }
            }
        }

        viewModelScope.launch {
            appDataStore.dailyReminderEnabled.collect { enabled ->
                _uiState.update { it.copy(dailyReminderEnabled = enabled) }
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
        val currentMonth = YearMonth.now()
        val currentMonthTransactions = realTransactions.filter { tx ->
            YearMonth.from(tx.date) == currentMonth
        }

        val totalIncome = currentMonthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val totalExpense = currentMonthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        // Weekly breakdown: map transactions to Mon-Sun of current week
        val daysOfWeek = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val weeklyIncomeMap = daysOfWeek.associateWith { 0f }.toMutableMap()
        val weeklyExpenseMap = daysOfWeek.associateWith { 0f }.toMutableMap()

        val today = LocalDate.now()
        val startOfWeek = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val endOfWeek = startOfWeek.plusDays(6)

        val currentWeekTransactions = realTransactions.filter { tx ->
            !tx.date.isBefore(startOfWeek) && !tx.date.isAfter(endOfWeek)
        }

        currentWeekTransactions.forEach { tx ->
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

        // Category breakdown for expenses of current month
        val expenseTransactions = currentMonthTransactions.filter { it.type == TransactionType.EXPENSE }
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
            emptyList()
        }

        _uiState.update { state ->
            state.copy(
                recentTransactions = realTransactions.take(8),
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
        viewModelScope.launch {
            syncManager.syncPendingTransactions()
        }
    }

    fun exportPdf(context: Context) {
        viewModelScope.launch {
            val transactions = transactionRepository.getAllTransactions().first()
            PdfGenerator.export(context, transactions)
        }
    }

    fun syncAndOpenSpreadsheet(context: Context) {
        viewModelScope.launch {
            if (_uiState.value.sheetsUrl.isEmpty()) {
                android.widget.Toast.makeText(context, "Spreadsheet URL belum diatur di Profil Anda.", android.widget.Toast.LENGTH_LONG).show()
                return@launch
            }
            android.widget.Toast.makeText(context, "Sinkronisasi ke Google Sheets...", android.widget.Toast.LENGTH_SHORT).show()
            val result = syncManager.syncPendingTransactions()
            result.fold(
                onSuccess = { success ->
                    if (success) {
                        android.widget.Toast.makeText(context, "Sinkronisasi berhasil!", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Tidak ada transaksi baru untuk disinkronkan.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    openSpreadsheetUrl(context)
                },
                onFailure = { e ->
                    android.widget.Toast.makeText(context, "Gagal sinkronisasi: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    openSpreadsheetUrl(context)
                }
            )
        }
    }

    private fun openSpreadsheetUrl(context: Context) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(_uiState.value.sheetsUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Gagal membuka link spreadsheet.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}