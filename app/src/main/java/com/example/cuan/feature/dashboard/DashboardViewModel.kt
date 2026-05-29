package com.example.cuan.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionType
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
    val anomalyMessage: String? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val appDataStore: AppDataStore
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

        // Generate sample data for demo
        generateSampleTransactions()
        checkForAnomalies()
    }

    private fun generateSampleTransactions() {
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

        _uiState.update { state ->
            val totalIncome = sampleTransactions
                .filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount }
            val totalExpense = sampleTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }

            state.copy(
                recentTransactions = sampleTransactions,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                isLoading = false
            )
        }
    }

    private fun checkForAnomalies() {
        // Simple anomaly detection - in real app would use UseCase
        val totalExpense = _uiState.value.totalExpense
        if (totalExpense > 2000000) {
            _uiState.update {
                it.copy(anomalyMessage = "Pengeluaran bulan ini sudah melebihi Rp 2 juta. Hati-hati!")
            }
        }
    }

    fun dismissAnomaly() {
        _uiState.update { it.copy(anomalyMessage = null) }
    }

    fun refreshData() {
        loadData()
    }
}