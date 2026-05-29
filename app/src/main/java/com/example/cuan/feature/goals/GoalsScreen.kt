package com.example.cuan.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.feature.goals.components.AddGoalBottomSheetComponent
import com.example.cuan.feature.goals.components.GoalCardComponent
import com.example.cuan.ui.components.EmptyStateComponent
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.OnAccent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary

/**
 * Goals Screen (F-10) - Minimalist & Professional redesign.
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
                title = { 
                    Text(
                        text = "Target Tabungan", 
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = Accent,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.padding(bottom = 76.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Target", tint = OnAccent)
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
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(uiState.goals) { goal ->
                    GoalCardComponent(
                        goal = goal,
                        onEdit = { /* TODO */ },
                        onDelete = { viewModel.deleteGoal(goal.id) }
                    )
                }
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
            AddGoalBottomSheetComponent(
                onSave = { name, targetAmount, currentAmount, deadline ->
                    viewModel.addGoal(name, targetAmount, currentAmount, deadline)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }
}