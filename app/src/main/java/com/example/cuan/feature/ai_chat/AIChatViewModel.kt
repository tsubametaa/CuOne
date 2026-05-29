package com.example.cuan.feature.ai_chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val appDataStore: AppDataStore
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

        // Simulate AI response
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Add loading message
            _uiState.update { state ->
                state.copy(messages = state.messages + ChatMessage("", isFromUser = false, isLoading = true))
            }

            // Simulate AI processing delay
            delay(1500)

            // Generate response (in real app would call OpenRouter)
            val response = generateAIResponse(userMessage)

            // Remove loading and add response
            _uiState.update { state ->
                val messagesWithoutLoading = state.messages.dropLast(1)
                state.copy(
                    messages = messagesWithoutLoading + ChatMessage(response, isFromUser = false),
                    isLoading = false
                )
            }
        }
    }

    private fun generateAIResponse(userMessage: String): String {
        val lowerMessage = userMessage.lowercase()

        return when {
            lowerMessage.contains("pengeluaran minggu") || lowerMessage.contains("minggu ini") -> {
                "Berdasarkan data minggu ini, total pengeluaran kamu sekitar Rp 485.000. terbesar adalah untuk makan (Rp 250.000) dan transport (Rp 150.000)."
            }
            lowerMessage.contains("kategori") && lowerMessage.contains("besar") -> {
                "Bulan ini, kategori terbesar adalah:\n\n1. Makan: Rp 850.000 (34%)\n2. Belanja: Rp 600.000 (24%)\n3. Transport: Rp 450.000 (18%)\n\nUntuk kategori makan, kamu bisa coba memasak sendiri di rumah untuk menghemat sekitar 30-40%."
            }
            lowerMessage.contains("tren") || lowerMessage.contains("3 bulan") -> {
                "Tren keuangan 3 bulan terakhir:\n\n- Bulan Maret: Pendapatan Rp 5.5jt, Pengeluaran Rp 2.3jt\n- Bulan April: Pendapatan Rp 5.5jt, Pengeluaran Rp 2.8jt\n- Bulan Mei: Pendapatan Rp 5.5jt, Pengeluaran Rp 2.5jt\n\nPengeluaran kamu cukup stabil, tapi ada kenaikan dibanding bulan lalu."
            }
            lowerMessage.contains("hemat") || lowerMessage.contains("irit") -> {
                "Berdasarkan analysismu, beberapa tips hemat:\n\n1. **Makan** - Bisa hemat Rp 250rb/bulan dengan memasak sendiri\n2. **Hiburan** - Langganan streaming bisa digabung dengan keluarga\n3. **Belanja** - Beli groceries mingguan daripada harian\n\nMau aku bantu hitung target penghematan bulan depan?"
            }
            else -> {
                "Pertanyaan yang bagus! Aku bisa membantu kamu menganalisis keuangan dengan lebih detail. Coba tanya tentang:\n\n- Total pengeluaran minggu ini\n- Kategori terbesar bulan ini\n- Tips hemat dinheiro\n\nAda yang ingin kamu tanya lagi?"
            }
        }
    }
}