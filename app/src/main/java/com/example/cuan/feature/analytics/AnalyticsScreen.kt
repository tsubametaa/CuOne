package com.example.cuan.feature.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.feature.analytics.components.CategoryBreakdownComponent
import com.example.cuan.feature.analytics.components.DailyAverageComponent
import com.example.cuan.feature.analytics.components.MonthSelectorComponent
import com.example.cuan.feature.analytics.components.MonthlySummaryComponent
import com.example.cuan.feature.analytics.components.TopExpensesComponent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary

 // Analytics Screen (F-07)//
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Analitik", 
                        color = OnBackground,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Month selector
            item {
                MonthSelectorComponent(
                    months = uiState.availableMonths,
                    selectedMonth = uiState.selectedMonth,
                    onMonthSelected = viewModel::selectMonth,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Monthly Summary
            item {
                MonthlySummaryComponent(
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    comparedToPrevMonth = uiState.comparedToPrevMonth
                )
            }

            // Category Breakdown
            item {
                CategoryBreakdownComponent(
                    categories = uiState.categoryBreakdown
                )
            }

            // Daily Average & Projection
            item {
                DailyAverageComponent(
                    dailyAverage = uiState.dailyAverage,
                    projectedMonthEnd = uiState.projectedMonthEnd,
                    daysPassed = uiState.daysPassed,
                    totalDays = uiState.totalDays
                )
            }

            // Top Expenses
            item {
                TopExpensesComponent(
                    topExpenses = uiState.topExpenses
                )
            }
        }
    }
}