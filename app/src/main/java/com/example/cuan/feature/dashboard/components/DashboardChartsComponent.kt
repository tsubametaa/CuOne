package com.example.cuan.feature.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.IncomeGreen
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.TextSecondary

// Custom colors based on the design request
val ChartIncomeBlue = IncomeGreen
val ChartExpenseRed = Accent
val CategoryColors = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Green
    Color(0xFFF59E0B), // Orange
    Color(0xFFEF4444)  // Red
)

data class DailyChartData(
    val dayLabel: String,
    val incomeValue: Float,
    val expenseValue: Float
)

data class CategoryChartData(
    val label: String,
    val percentage: Float,
    val color: Color
)

@Composable
fun WeeklyAnalysisChartCard(
    data: List<DailyChartData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Analisis Mingguan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Bar Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = 8.dp.toPx()
                    val cornerRadius = 4.dp.toPx()
                    val spacing = 6.dp.toPx()
                    
                    val maxValue = data.maxOfOrNull { maxOf(it.incomeValue, it.expenseValue) } ?: 1f
                    val chartHeight = size.height
                    
                    val totalDays = data.size
                    val sectionWidth = size.width / totalDays

                    data.forEachIndexed { index, dayData ->
                        val sectionCenter = (index * sectionWidth) + (sectionWidth / 2)
                        
                        val incomeHeight = (dayData.incomeValue / maxValue) * chartHeight
                        val expenseHeight = (dayData.expenseValue / maxValue) * chartHeight

                        // Draw Income Bar
                        val incomeX = sectionCenter - spacing
                        drawLine(
                            color = ChartIncomeBlue,
                            start = Offset(incomeX, chartHeight),
                            end = Offset(incomeX, chartHeight - incomeHeight),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )

                        // Draw Expense Bar
                        val expenseX = sectionCenter + spacing
                        drawLine(
                            color = ChartExpenseRed,
                            start = Offset(expenseX, chartHeight),
                            end = Offset(expenseX, chartHeight - expenseHeight),
                            strokeWidth = barWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { dayData ->
                    Text(
                        text = dayData.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = ChartIncomeBlue, label = "Income")
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem(color = ChartExpenseRed, label = "Expense")
            }
        }
    }
}

@Composable
fun ExpenseCategoryChartCard(
    totalAmountStr: String,
    data: List<CategoryChartData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Kategori Pengeluaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        val strokeWidth = 32f

                        data.forEach { category ->
                            val sweepAngle = (category.percentage / 100f) * 360f
                            drawArc(
                                color = category.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle - 4f, // -4f for small gap
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                        Text(
                            text = totalAmountStr,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend List
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    data.forEach { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(category.color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnBackground,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${category.percentage.toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = OnBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardActionButtons(
    onExportPdfClick: () -> Unit,
    onSpreadsheetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Export PDF Button
        Row(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, ChartIncomeBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .background(ChartIncomeBlue.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .clickable(onClick = onExportPdfClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = ChartIncomeBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Export PDF",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = ChartIncomeBlue
            )
        }

        // Spreadsheet Button
        Row(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Secondary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .background(Secondary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .clickable(onClick = onSpreadsheetClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Spreadsheet",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Secondary
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
    }
}
