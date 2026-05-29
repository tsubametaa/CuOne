package com.example.cuan.feature.transaction.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.data.model.Transaction
import com.example.cuan.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
                val sortedTransactions = realTransactions.sortedByDescending { it.timeMillis }

                _uiState.update { state ->
                    val filtered = if (state.searchQuery.isBlank()) {
                        sortedTransactions
                    } else {
                        sortedTransactions.filter {
                            it.note.contains(state.searchQuery, ignoreCase = true) ||
                            it.category.contains(state.searchQuery, ignoreCase = true)
                        }
                    }
                    state.copy(
                        transactions = sortedTransactions,
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