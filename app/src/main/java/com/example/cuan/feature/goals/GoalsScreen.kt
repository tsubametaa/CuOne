package com.example.cuan.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.core.utils.CurrencyUtils
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.data.model.SavingsGoal
import com.example.cuan.ui.components.EmptyStateComponent
import com.example.cuan.ui.components.PrimaryButtonComponent
import com.example.cuan.ui.components.SecondaryButtonComponent
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.IncomeGreen
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary

/**
 * Goals Screen (F-10)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Target Tabungan", color = OnSecondary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Accent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Target", tint = OnSecondary)
            }
        }
    ) { paddingValues ->
        if (uiState.goals.isEmpty()) {
            EmptyStateComponent(
                icon = Icons.Default.TrackChanges,
                title = "Belum ada target tabungan",
                subtitle = "Buat target tabungan untuk menabung lebih konsisten",
                actionText = "Buat Target",
                onAction = { showAddSheet = true },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onEdit = { /* TODO */ },
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Add Goal Bottom Sheet
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = Background
        ) {
            AddGoalBottomSheet(
                onSave = { name, targetAmount, currentAmount, deadline ->
                    viewModel.addGoal(name, targetAmount, currentAmount, deadline)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }
}

@Composable
fun GoalCard(
    goal: SavingsGoal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Name and percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(goal.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (goal.isCompleted) IncomeGreen else Secondary,
                trackColor = Background
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Remaining amount and deadline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rp ${CurrencyUtils.formatNumber(goal.remainingAmount)} lagi",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                goal.deadline?.let { deadline ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = DateUtils.formatShortDate(deadline),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Daily savings needed
            if (!goal.isCompleted && goal.dailySavingsNeeded > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hemat Rp ${CurrencyUtils.formatShort(goal.dailySavingsNeeded)}/hari untuk tepat waktu",
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    onSave: (String, Long, Long, java.time.LocalDate?) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var currentAmount by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf<java.time.LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Buat Target Tabungan",
            style = MaterialTheme.typography.headlineSmall
        )

        // Name input
        androidx.compose.material3.OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama target") },
            placeholder = { Text("Contoh: Liburan ke Bali") },
            modifier = Modifier.fillMaxWidth()
        )

        // Target amount
        androidx.compose.material3.OutlinedTextField(
            value = targetAmount,
            onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
            label = { Text("Nominal target") },
            prefix = { Text("Rp ") },
            modifier = Modifier.fillMaxWidth()
        )

        // Current amount
        androidx.compose.material3.OutlinedTextField(
            value = currentAmount,
            onValueChange = { currentAmount = it.filter { c -> c.isDigit() } },
            label = { Text("Sudah ditabung") },
            prefix = { Text("Rp ") },
            modifier = Modifier.fillMaxWidth()
        )

        // Deadline
        androidx.compose.material3.OutlinedTextField(
            value = deadline?.let { DateUtils.formatDate(it) } ?: "",
            onValueChange = { },
            label = { Text("Deadline (opsional)") },
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Calculation preview
        if (targetAmount.isNotEmpty() && deadline != null) {
            val target = targetAmount.toLongOrNull() ?: 0
            val current = currentAmount.toLongOrNull() ?: 0
            val remaining = target - current
            val days = DateUtils.daysUntil(deadline!!)
            val daily = if (days > 0) remaining / days else 0

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Harus menabung Rp ${CurrencyUtils.formatShort(daily)}/hari",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent
                    )
                }
            }
        }

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
                    val target = targetAmount.toLongOrNull() ?: 0
                    val current = currentAmount.toLongOrNull() ?: 0
                    onSave(name, target, current, deadline)
                },
                enabled = name.isNotEmpty() && targetAmount.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}