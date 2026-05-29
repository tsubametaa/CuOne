package com.example.cuan.feature.transaction.add.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary

@Composable
fun TransactionNoteCardComponent(
    note: String,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BackgroundVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Catatan",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            
            OutlinedTextField(
                value = note,
                onValueChange = {
                    if (it.length <= 200) onNoteChange(it)
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                placeholder = { Text("Tambahkan deskripsi...", color = TextSecondary.copy(alpha = 0.6f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = BackgroundVariant,
                    focusedLabelColor = Secondary,
                    cursorColor = Secondary,
                    focusedContainerColor = Background,
                    unfocusedContainerColor = Background,
                    focusedTextColor = TextSecondary,
                    unfocusedTextColor = TextSecondary
                )
            )
            
            Text(
                text = "${note.length} / 200",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
