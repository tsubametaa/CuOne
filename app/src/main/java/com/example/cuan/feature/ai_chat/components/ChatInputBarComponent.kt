package com.example.cuan.feature.ai_chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnAccent
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary

 //* Sleek, minimal bottom chat input bar component. //
@Composable
fun ChatInputBarComponent(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val canSend = inputText.isNotBlank() && !isLoading
    val sendButtonColor = if (canSend) Accent else BackgroundVariant
    val sendIconColor = if (canSend) OnAccent else TextSecondary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChange,
            placeholder = { Text("Tanyakan sesuatu ke AI...", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedContainerColor = BackgroundVariant,
                unfocusedContainerColor = BackgroundVariant,
                disabledContainerColor = BackgroundVariant
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onSendClick,
            enabled = canSend,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(sendButtonColor)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Kirim",
                tint = sendIconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
