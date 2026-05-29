package com.example.cuan.feature.analytics

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.core.utils.CurrencyUtils
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.IncomeGreen
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.SecondaryContainer
import com.example.cuan.ui.theme.TextSecondary
import java.time.YearMonth

/**
 * Analytics Screen (F-07)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analitik", color = OnSecondary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month selector
            item {
                MonthSelector(
                    months = uiState.availableMonths,
                    selectedMonth = uiState.selectedMonth,
                    onMonthSelected = viewModel::selectMonth
                )
            }

            // Monthly Summary
            item {
                MonthlySummaryCard(
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    comparedToPrevMonth = uiState.comparedToPrevMonth
                )
            }

            // Category Breakdown
            item {
                CategoryBreakdownCard(
                    categories = uiState.categoryBreakdown
                )
            }

            // Daily Average & Projection
            item {
                DailyAverageCard(
                    dailyAverage = uiState.dailyAverage,
                    projectedMonthEnd = uiState.projectedMonthEnd,
                    daysPassed = uiState.daysPassed,
                    totalDays = uiState.totalDays
                )
            }

            // Top Expenses
            item {
                TopExpensesCard(
                    topExpenses = uiState.topExpenses
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun MonthSelector(
    months: List<YearMonth>,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(months) { month ->
            FilterChip(
                selected = month == selectedMonth,
                onClick = { onMonthSelected(month) },
                label = { Text(DateUtils.formatShortMonth(month)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Secondary,
                    selectedLabelColor = OnSecondary,
                    containerColor = BackgroundVariant,
                    labelColor = OnBackground
                )
            )
        }
    }
}

@Composable
fun MonthlySummaryCard(
    totalIncome: Long,
    totalExpense: Long,
    comparedToPrevMonth: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ringkasan Bulanan",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Column {
                    Text(
                        text = "Pemasukan",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(totalIncome),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = IncomeGreen
                    )
                }

                // Expense
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Pengeluaran",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(totalExpense),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comparison
            val balance = totalIncome - totalExpense
            val balanceColor = if (balance >= 0) IncomeGreen else Accent
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saldo: ${CurrencyUtils.formatRupiah(balance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = balanceColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    if (comparedToPrevMonth >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (comparedToPrevMonth >= 0) IncomeGreen else Accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = CurrencyUtils.formatComparison(comparedToPrevMonth),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (comparedToPrevMonth >= 0) IncomeGreen else Accent
                )
                Text(
                    text = " vs bulan lalu",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

data class CategoryBreakdownItem(
    val category: String,
    val amount: Long,
    val percentage: Float
)

@Composable
fun CategoryBreakdownCard(categories: List<CategoryBreakdownItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Pengeluaran per Kategori",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            val total = categories.sumOf { it.amount }.toFloat()
            
            if (total > 0) {
                categories.forEach { item ->
                    CategoryBreakdownRow(
                        category = item.category,
                        amount = item.amount,
                        percentage = if (total > 0) item.amount / total else 0f
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "Belum ada data",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownRow(
    category: String,
    amount: Long,
    percentage: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = CurrencyUtils.formatRupiah(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Secondary,
            trackColor = Background
        )
    }
}

@Composable
fun DailyAverageCard(
    dailyAverage: Long,
    projectedMonthEnd: Long,
    daysPassed: Int,
    totalDays: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rata-rata & Proyeksi",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Rata-rata/hari",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(dailyAverage),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Proyeksi akhir bulan",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(projectedMonthEnd),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (projectedMonthEnd >= 0) IncomeGreen else Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            val progress = daysPassed.toFloat() / totalDays.toFloat()
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Secondary,
                trackColor = Background
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Hari ke-$daysPassed dari $totalDays hari",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun TopExpensesCard(topExpenses: List<CategoryBreakdownItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Top 5 Pengeluaran Terbesar",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            topExpenses.forEachIndexed { index, expense ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Number
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Secondary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = expense.category,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = CurrencyUtils.formatRupiah(expense.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Accent
                    )
                }
            }
        }
    }
}