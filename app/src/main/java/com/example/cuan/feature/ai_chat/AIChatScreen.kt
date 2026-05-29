package com.example.cuan.feature.ai_chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.feature.ai_chat.components.ChatBubbleComponent
import com.example.cuan.feature.ai_chat.components.ChatInputBarComponent
import com.example.cuan.feature.ai_chat.components.SuggestedQuestionsComponent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary

/**
 * AI Chat Screen (F-08) - Minimalist & Professional redesign.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIChatScreen(
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    val isKeyboardVisible = WindowInsets.isImeVisible
    val bottomPadding = if (isKeyboardVisible) 0.dp else 80.dp

    // Auto-scroll to bottom on new messages
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Asisten AI", 
                            color = OnBackground,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Didukung Nemotron",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .imePadding()
        ) {
            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Show suggested questions only when chat is empty
                if (uiState.messages.isEmpty()) {
                    item {
                        SuggestedQuestionsComponent(
                            questions = uiState.suggestedQuestions,
                            onQuestionClick = viewModel::sendMessage
                        )
                    }
                }

                // Chat messages
                items(uiState.messages) { message ->
                    ChatBubbleComponent(
                        message = message.text,
                        isFromUser = message.isFromUser,
                        isLoading = message.isLoading
                    )
                }
            }

            // Input bar
            ChatInputBarComponent(
                inputText = uiState.inputText,
                onInputChange = viewModel::updateInput,
                onSendClick = viewModel::sendMessage,
                isLoading = uiState.isLoading,
                modifier = Modifier.padding(bottom = bottomPadding)
            )
        }
    }
}