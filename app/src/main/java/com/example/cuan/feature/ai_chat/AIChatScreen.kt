package com.example.cuan.feature.ai_chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.SecondaryContainer
import com.example.cuan.ui.theme.TextSecondary

/**
 * AI Chat Screen (F-08)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

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
                        Text("Asisten AI", color = OnSecondary)
                        Text(
                            "Didukung Nemotron",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSecondary.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
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
                        SuggestedQuestions(
                            questions = uiState.suggestedQuestions,
                            onQuestionClick = viewModel::sendMessage
                        )
                    }
                }

                // Chat messages
                items(uiState.messages) { message ->
                    ChatBubble(
                        message = message.text,
                        isFromUser = message.isFromUser,
                        isLoading = message.isLoading
                    )
                }
            }

            // Input bar
            ChatInputBar(
                inputText = uiState.inputText,
                onInputChange = viewModel::updateInput,
                onSendClick = viewModel::sendMessage,
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
fun SuggestedQuestions(
    questions: List<String>,
    onQuestionClick: (String) -> Unit
) {
    Column {
        Text(
            text = "Pertanyaan Saran",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(questions) { question ->
                FilterChip(
                    selected = false,
                    onClick = { onQuestionClick(question) },
                    label = { Text(question, style = MaterialTheme.typography.bodySmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SecondaryContainer,
                        labelColor = OnBackground
                    )
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: String,
    isFromUser: Boolean,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromUser) Alignment.End else Alignment.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromUser) Secondary else BackgroundVariant
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromUser) 16.dp else 4.dp,
                bottomEnd = if (isFromUser) 4.dp else 16.dp
            )
        ) {
            if (isLoading) {
                // Loading dots animation placeholder
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(TextSecondary.copy(alpha = 0.5f))
                        )
                    }
                }
            } else {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromUser) OnSecondary else OnBackground,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { Text("Tanyakan tentang keuangan...") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = BackgroundVariant,
                focusedContainerColor = BackgroundVariant,
                unfocusedContainerColor = BackgroundVariant
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSendClick,
            enabled = inputText.isNotEmpty() && !isLoading,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (inputText.isNotEmpty()) Accent else BackgroundVariant)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Kirim",
                tint = if (inputText.isNotEmpty()) OnAccent else TextSecondary
            )
        }
    }
}

private val OnAccent = com.example.cuan.ui.theme.OnAccent