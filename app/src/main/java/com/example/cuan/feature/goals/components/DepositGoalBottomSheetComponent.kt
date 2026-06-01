package com.example.cuan.feature.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cuan.core.utils.CurrencyUtils
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.core.utils.IndonesianCurrencyVisualTransformation
import com.example.cuan.data.model.SavingsGoal
import com.example.cuan.ui.components.PrimaryButtonComponent
import com.example.cuan.ui.components.SecondaryButtonComponent
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.IncomeGreen
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary
import java.time.LocalDate
import java.util.Calendar

// Bottom sheet modal for depositing savings into an existing target/goal.

@Composable
fun DepositGoalBottomSheetComponent(
    goal: SavingsGoal,
    onSave: (Long, LocalDate) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var depositAmountStr by remember { mutableStateOf("") }
    var depositDate by remember { mutableStateOf(LocalDate.now()) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                depositDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    val depositAmount = depositAmountStr.toLongOrNull() ?: 0L
    val remainingAmountAfter = (goal.targetAmount - (goal.currentAmount + depositAmount)).coerceAtLeast(0L)
    val isGoalCompletedWithDeposit = (goal.currentAmount + depositAmount) >= goal.targetAmount

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Menabung untuk Target",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = OnBackground
        )

        Text(
            text = goal.name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Secondary
            )
        )

        // Deposit amount input
        OutlinedTextField(
            value = depositAmountStr,
            onValueChange = { depositAmountStr = it.filter { c -> c.isDigit() } },
            label = { Text("Nominal yang Ditabung") },
            prefix = { Text("Rp ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = IndonesianCurrencyVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = BackgroundVariant,
                focusedLabelColor = Secondary
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Deposit Date picker
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
        ) {
            OutlinedTextField(
                value = DateUtils.formatDate(depositDate),
                onValueChange = { },
                label = { Text("Tanggal Menabung") },
                readOnly = true,
                enabled = false,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Pilih Tanggal",
                        tint = Secondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = OnBackground,
                    disabledBorderColor = BackgroundVariant,
                    disabledLabelColor = TextSecondary,
                    disabledTrailingIconColor = TextSecondary,
                    disabledPlaceholderColor = TextSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Financial Projections Card (Dynamic Projections)
        Card(
            colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isGoalCompletedWithDeposit) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = IncomeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Luar biasa! Target tabungan Anda terpenuhi dengan tabungan ini! 🎉",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = IncomeGreen
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Berapa banyak lagi: Rp ${CurrencyUtils.formatNumber(remainingAmountAfter)} lagi",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = OnBackground
                            )

                            // How long more calculation
                            val deadline = goal.deadline
                            if (deadline != null) {
                                val daysRemaining = DateUtils.daysUntil(deadline)
                                if (daysRemaining > 0) {
                                    val oldDaily = (goal.targetAmount - goal.currentAmount) / daysRemaining
                                    val newDaily = remainingAmountAfter / daysRemaining
                                    
                                    Text(
                                        text = "Sisa waktu: $daysRemaining hari lagi (${DateUtils.formatShortDate(deadline)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    
                                    if (depositAmount > 0) {
                                        Text(
                                            text = "Beban menabung harian Anda berkurang dari Rp ${CurrencyUtils.formatNumber(oldDaily)}/hari menjadi Rp ${CurrencyUtils.formatNumber(newDaily)}/hari.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = Accent
                                        )
                                    }
                                }
                            }
                            
                            if (depositAmount > 0) {
                                val estDays = (remainingAmountAfter + depositAmount - 1) / depositAmount
                                Text(
                                    text = "Jika Anda menabung Rp ${CurrencyUtils.formatNumber(depositAmount)} setiap hari, target akan tercapai dalam $estDays hari lagi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SecondaryButtonComponent(
                text = "Batal",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )
            PrimaryButtonComponent(
                text = "Simpan",
                onClick = {
                    if (depositAmount > 0) {
                        onSave(depositAmount, depositDate)
                    }
                },
                enabled = depositAmount > 0,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
