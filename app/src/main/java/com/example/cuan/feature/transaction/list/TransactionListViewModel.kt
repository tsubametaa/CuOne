package com.example.cuan.feature.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.model.TransactionSource
import com.example.cuan.data.model.TransactionType
import com.example.cuan.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionListUiState())
    val uiState: StateFlow<TransactionListUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            transactionRepository.getAllTransactions().collect { realTransactions ->
                val now = System.currentTimeMillis()
                val today = LocalDate.now()
                
                val sampleTransactions = listOf(
                    Transaction("1", 5000000, TransactionType.INCOME, "Gaji", "Gaji bulan Mei", today.minusDays(1), now - 86400000, TransactionSource.MANUAL, true),
                    Transaction("2", 150000, TransactionType.EXPENSE, "Makan", "Makan siang di mall", today, now, TransactionSource.MANUAL, true),
                    Transaction("3", 50000, TransactionType.EXPENSE, "Transport", "Grab ke kantor", today, now - 3600000, TransactionSource.MANUAL, true),
                    Transaction("4", 250000, TransactionType.EXPENSE, "Belanja", "Belanja groceries", today.minusDays(2), now - 172800000, TransactionSource.SCAN, true),
                    Transaction("5", 75000, TransactionType.EXPENSE, "Hiburan", "Nonton film", today.minusDays(3), now - 259200000, TransactionSource.MANUAL, true),
                    Transaction("6", 200000, TransactionType.EXPENSE, "Tagihan", "Listrik bulan Mei", today.minusDays(4), now - 345600000, TransactionSource.MANUAL, true),
                    Transaction("7", 3500000, TransactionType.INCOME, "Freelance", "Proyek website", today.minusDays(5), now - 432000000, TransactionSource.FREE_TEXT, true),
                    Transaction("8", 45000, TransactionType.EXPENSE, "Makan", "Kopi dan snack", today.minusDays(6), now - 518400000, TransactionSource.MANUAL, true),
                    Transaction("9", 120000, TransactionType.EXPENSE, "Kesehatan", "Obat flu", today.minusDays(7), now - 604800000, TransactionSource.MANUAL, true),
                    Transaction("10", 150000, TransactionType.EXPENSE, "Transport", "Bensin", today.minusDays(8), now - 691200000, TransactionSource.MANUAL, true)
                )

                val combinedTransactions = (realTransactions + sampleTransactions).distinctBy { it.id }
                    .sortedByDescending { it.timeMillis }

                _uiState.update { state ->
                    val filtered = if (state.searchQuery.isBlank()) {
                        combinedTransactions
                    } else {
                        combinedTransactions.filter {
                            it.note.contains(state.searchQuery, ignoreCase = true) ||
                            it.category.contains(state.searchQuery, ignoreCase = true)
                        }
                    }
                    state.copy(
                        transactions = combinedTransactions,
                        filteredTransactions = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = if (query.isBlank()) {
                state.transactions
            } else {
                state.transactions.filter {
                    it.note.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            }
            state.copy(searchQuery = query, filteredTransactions = filtered)
        }
    }
}