package com.example.cuan.feature.transaction.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.data.model.TransactionCategories
import com.example.cuan.data.model.TransactionSource
import com.example.cuan.data.model.TransactionType
import com.example.cuan.ui.components.CategoryChipComponent
import com.example.cuan.ui.components.PrimaryButtonComponent
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.IncomeGreen
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.SecondaryContainer
import com.example.cuan.ui.theme.SurfaceError
import com.example.cuan.ui.theme.TextSecondary

/**
 * Add Transaction Screen (F-03)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Transaksi Baru",
                        color = OnSecondary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = OnSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Secondary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Input
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::updateAmount,
                label = { Text("Nominal") },
                prefix = { Text("Rp ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = BackgroundVariant,
                    focusedLabelColor = Secondary,
                    cursorColor = Secondary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Transaction Type Toggle
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = uiState.type == TransactionType.EXPENSE,
                    onClick = { viewModel.updateType(TransactionType.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SurfaceError,
                        activeContentColor = Accent
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Pengeluaran")
                    }
                }
                SegmentedButton(
                    selected = uiState.type == TransactionType.INCOME,
                    onClick = { viewModel.updateType(TransactionType.INCOME) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = SecondaryContainer,
                        activeContentColor = Secondary
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Pemasukan")
                    }
                }
            }

            // Category Chips
            Text(
                text = "Kategori",
                style = MaterialTheme.typography.labelLarge
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = TransactionCategories.getCategoriesForType(uiState.type)
                val icons = getCategoryIcons()
                categories.forEach { category ->
                    CategoryChipComponent(
                        label = category,
                        icon = icons[category] ?: Icons.Default.Receipt,
                        isSelected = uiState.category == category,
                        onClick = { viewModel.updateCategory(category) }
                    )
                }
            }

            // Note Input
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::updateNote,
                label = { Text("Catatan (opsional)") },
                leadingIcon = {
                    Icon(Icons.Default.Create, contentDescription = null)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = BackgroundVariant,
                    focusedLabelColor = Secondary,
                    cursorColor = Secondary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Date Input
            OutlinedTextField(
                value = uiState.date,
                onValueChange = { },
                label = { Text("Tanggal") },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                },
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = BackgroundVariant,
                    focusedLabelColor = Secondary,
                    cursorColor = Secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            PrimaryButtonComponent(
                text = "Simpan",
                onClick = { viewModel.saveTransaction() },
                icon = Icons.Default.CheckCircle,
                enabled = uiState.amount.isNotEmpty() && uiState.category.isNotEmpty(),
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.updateDate(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.padding(0.dp) // Placeholder - using clickable from Column
    )
}

fun getCategoryIcons(): Map<String, ImageVector> = mapOf(
    "Makan" to Icons.Default.Fastfood,
    "Transport" to Icons.Default.Home,
    "Belanja" to Icons.Default.Receipt,
    "Hiburan" to Icons.Default.LocalActivity,
    "Kesehatan" to Icons.Default.HealthAndSafety,
    "Tagihan" to Icons.Default.Receipt,
    "Lainnya" to Icons.Default.MoreHoriz,
    "Gaji" to Icons.AutoMirrored.Filled.TrendingUp,
    "Freelance" to Icons.AutoMirrored.Filled.TrendingUp,
    "Bisnis" to Icons.AutoMirrored.Filled.TrendingUp,
    "Investasi" to Icons.AutoMirrored.Filled.TrendingUp,
    "Hadiah" to Icons.AutoMirrored.Filled.TrendingUp,
    "Lainnya" to Icons.AutoMirrored.Filled.TrendingUp
)