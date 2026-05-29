package com.example.cuan.feature.goals.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.cuan.core.utils.IndonesianCurrencyVisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cuan.core.utils.CurrencyUtils
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.ui.components.PrimaryButtonComponent
import com.example.cuan.ui.components.SecondaryButtonComponent
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary
import java.time.LocalDate
import java.util.Calendar

/**
 * Bottom sheet component for adding a new saving target/goal.
 */
@Composable
fun AddGoalBottomSheetComponent(
    onSave: (String, Long, Long, LocalDate?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var currentAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<LocalDate?>(null) }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                deadline = LocalDate.of(year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Buat Target Tabungan",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = OnBackground
        )

        // Name input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Target") },
            placeholder = { Text("Contoh: Beli Laptop Kerja") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secondary,
                unfocusedBorderColor = BackgroundVariant,
                focusedLabelColor = Secondary
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Target amount
        OutlinedTextField(
            value = targetAmount,
            onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
            label = { Text("Nominal Target") },
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

        // Current amount
        OutlinedTextField(
            value = currentAmount,
            onValueChange = { currentAmount = it.filter { c -> c.isDigit() } },
            label = { Text("Sudah Ditabung (Opsional)") },
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

        // Deadline (Clickable Field)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
        ) {
            OutlinedTextField(
                value = deadline?.let { DateUtils.formatDate(it) } ?: "",
                onValueChange = { },
                label = { Text("Deadline (Opsional)") },
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

        // Preview daily savings rate calculation
        if (targetAmount.isNotEmpty() && deadline != null) {
            val target = targetAmount.toLongOrNull() ?: 0L
            val current = currentAmount.toLongOrNull() ?: 0L
            val remaining = target - current
            val days = DateUtils.daysUntil(deadline!!)
            val daily = if (days > 0) remaining / days else 0L

            if (daily > 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Harus menabung sekitar Rp ${CurrencyUtils.formatNumber(daily)}/hari selama $days hari untuk mencapai target ini.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Accent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Buttons
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
                text = "Buat Target",
                onClick = {
                    val target = targetAmount.toLongOrNull() ?: 0L
                    val current = currentAmount.toLongOrNull() ?: 0L
                    onSave(name, target, current, deadline)
                },
                enabled = name.isNotEmpty() && targetAmount.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
