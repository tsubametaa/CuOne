package com.example.cuan.feature.ai_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.data.repository.AIRepository
import com.example.cuan.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val isLoading: Boolean = false
)

data class AIChatUiState(
    val inputText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val suggestedQuestions: List<String> = defaultSuggestedQuestions,
    val isLoading: Boolean = false,
    val userName: String = "",
    val occupation: String = ""
)

private val defaultSuggestedQuestions = listOf(
    "Berapa total pengeluaran minggu ini?",
    "Kategori apa yang paling besar bulan ini?",
    "Bagaimana tren keuanganku 3 bulan terakhir?",
    "Di mana aku bisa hemat lebih banyak?"
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val appDataStore: AppDataStore,
    private val transactionRepository: TransactionRepository,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userName = appDataStore.userName.first()
            val occupation = appDataStore.userOccupation.first()
            _uiState.update { it.copy(userName = userName, occupation = occupation) }
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage(text: String = _uiState.value.inputText) {
        if (text.isBlank()) return

        val userMessage = text.trim()
        
        // Add user message
        _uiState.update { state ->
            state.copy(
                inputText = "",
                messages = state.messages + ChatMessage(userMessage, isFromUser = true)
            )
        }

        // Send query to AI
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Add loading message placeholder
            _uiState.update { state ->
                state.copy(messages = state.messages + ChatMessage("", isFromUser = false, isLoading = true))
            }

            try {
                val apiKey = appDataStore.openRouterApiKey.first()
                val userName = appDataStore.userName.first()
                val occupation = appDataStore.userOccupation.first()
                
                // Get transaction context
                val transactions = transactionRepository.getAllTransactions().first()
                val transactionsJson = transactions.take(100).joinToString(separator = ",\n") { t ->
                    """{"tanggal":"${t.date}","tipe":"${t.type.name}","kategori":"${t.category}","nominal":${t.amount},"catatan":"${t.note}"}"""
                }

                val systemPrompt = """
                    Kamu adalah asisten keuangan personal yang membantu dan profesional.
                    Nama pengguna: $userName. Pekerjaan: $occupation.
                    Data transaksi: [
                    $transactionsJson
                    ]

                    Jawab pertanyaan pengguna berdasarkan data di atas secara ringkas dan
                    informatif dalam Bahasa Indonesia. Jika relevan, berikan saran praktis
                    yang spesifik. Jangan pernah mengarang data yang tidak ada dalam konteks.
                    Format nominal selalu dalam Rupiah (contoh: Rp 1.500.000).
                """.trimIndent()

                // Map UI messages history (excluding last loading placeholder and user message)
                val uiMessagesBeforeUserMessage = _uiState.value.messages.dropLast(2)
                val networkHistory = uiMessagesBeforeUserMessage.map { uiMsg ->
                    com.example.cuan.core.network.ChatMessage(
                        role = if (uiMsg.isFromUser) "user" else "assistant",
                        content = uiMsg.text
                    )
                }

                val systemMessage = com.example.cuan.core.network.ChatMessage(role = "system", content = systemPrompt)
                val historyWithSystem = listOf(systemMessage) + networkHistory

                val result = aiRepository.chatWithFinance(
                    prompt = userMessage,
                    history = historyWithSystem,
                    apiKey = apiKey
                )

                result.fold(
                    onSuccess = { aiResponse ->
                        _uiState.update { state ->
                            val messagesWithoutLoading = state.messages.dropLast(1)
                            state.copy(
                                messages = messagesWithoutLoading + ChatMessage(aiResponse, isFromUser = false),
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { state ->
                            val messagesWithoutLoading = state.messages.dropLast(1)
                            state.copy(
                                messages = messagesWithoutLoading + ChatMessage("Maaf, terjadi kesalahan: ${e.message}", isFromUser = false),
                                isLoading = false
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { state ->
                    val messagesWithoutLoading = state.messages.dropLast(1)
                    state.copy(
                        messages = messagesWithoutLoading + ChatMessage("Maaf, terjadi kesalahan: ${e.message}", isFromUser = false),
                        isLoading = false
                    )
                }
            }
        }
    }
}