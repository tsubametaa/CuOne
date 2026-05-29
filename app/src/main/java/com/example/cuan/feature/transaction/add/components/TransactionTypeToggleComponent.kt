package com.example.cuan.feature.transaction.add.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cuan.data.model.TransactionType
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.SecondaryContainer
import com.example.cuan.ui.theme.SurfaceError
import com.example.cuan.ui.theme.TextSecondary

@Composable
fun TransactionTypeToggleComponent(
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val isIncome = selectedType == TransactionType.INCOME
        val incomeBg = if (isIncome) SecondaryContainer else BackgroundVariant.copy(alpha = 0.5f)
        val incomeBorder = if (isIncome) Secondary else Color.Transparent
        val incomeTextColor = if (isIncome) Secondary else TextSecondary
        
        Card(
            onClick = { onTypeChange(TransactionType.INCOME) },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = incomeBg),
            border = BorderStroke(1.dp, incomeBorder),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pemasukan",
                    fontWeight = FontWeight.Bold,
                    color = incomeTextColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        val isExpense = selectedType == TransactionType.EXPENSE
        val expenseBg = if (isExpense) SurfaceError else BackgroundVariant.copy(alpha = 0.5f)
        val expenseBorder = if (isExpense) Accent else Color.Transparent
        val expenseTextColor = if (isExpense) Accent else TextSecondary
        
        Card(
            onClick = { onTypeChange(TransactionType.EXPENSE) },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = expenseBg),
            border = BorderStroke(1.dp, expenseBorder),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pengeluaran",
                    fontWeight = FontWeight.Bold,
                    color = expenseTextColor,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
