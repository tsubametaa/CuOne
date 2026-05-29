package com.example.cuan.feature.ai_chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.SecondaryContainer
import com.example.cuan.ui.theme.TextSecondary

// * Suggested questions component that displays a collection of modern flat question chips. //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedQuestionsComponent(
    questions: List<String>,
    onQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Rekomendasi Pertanyaan",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(questions) { question ->
                FilterChip(
                    selected = false,
                    onClick = { onQuestionClick(question) },
                    label = { 
                        Text(
                            text = question, 
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SecondaryContainer.copy(alpha = 0.6f),
                        labelColor = OnBackground
                    ),
                    border = null
                )
            }
        }
    }
}
