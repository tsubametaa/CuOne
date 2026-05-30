package com.example.cuan.feature.transaction.freetext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.data.model.TransactionType
import com.example.cuan.feature.transaction.add.components.TransactionAmountCardComponent
import com.example.cuan.feature.transaction.add.components.TransactionCategoryGridComponent
import com.example.cuan.feature.transaction.add.components.TransactionDateTimeRowComponent
import com.example.cuan.feature.transaction.add.components.TransactionNoteCardComponent
import com.example.cuan.feature.transaction.add.components.TransactionTypeToggleComponent
import com.example.cuan.ui.components.PrimaryButtonComponent
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnAccent
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.SecondaryContainer
import com.example.cuan.ui.theme.TextSecondary

// Free Text Input Screen (F-05) //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeTextScreen(
    viewModel: FreeTextViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    // Time picker dialog for confirmation
    val parsed = uiState.parsedData
    val timePickerDialog = remember(parsed?.hour, parsed?.minute) {
        android.app.TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                viewModel.updateParsedTime(selectedHour, selectedMinute)
            },
            parsed?.hour ?: java.time.LocalTime.now().hour,
            parsed?.minute ?: java.time.LocalTime.now().minute,
            false // 12-hour AM/PM format
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.showResultSheet) "Konfirmasi Transaksi" else "Input Teks",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.showResultSheet) {
                                viewModel.goBackToInput()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { paddingValues ->
        if (!uiState.showResultSheet || parsed == null) {
            // Text Input UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Large illustration icon
                Icon(
                    Icons.AutoMirrored.Filled.Message,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = SecondaryContainer
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input area
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::updateInputText,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = OnBackground),
                    placeholder = { Text("Contoh: tadi beli kopi 35rb, atau terima gaji 5 juta", color = TextSecondary.copy(alpha = 0.6f)) },
                    minLines = 4,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = BackgroundVariant,
                        focusedTextColor = OnBackground,
                        unfocusedTextColor = OnBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Process button
                if (uiState.isProcessing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Secondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "AI sedang memproses...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    PrimaryButtonComponent(
                        text = "Proses",
                        onClick = { viewModel.processText() },
                        icon = Icons.Default.SmartToy,
                        enabled = uiState.inputText.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Error message
                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Accent
                    )
                }
            }
        } else {
            // Confirmation form (exactly like ScanResultScreen)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Segmented Toggles
                TransactionTypeToggleComponent(
                    selectedType = parsed.type,
                    onTypeChange = viewModel::updateParsedType
                )

                // Amount Card
                TransactionAmountCardComponent(
                    amount = parsed.amount,
                    onAmountChange = viewModel::updateParsedAmount,
                    transactionType = parsed.type
                )

                // Category Grid
                TransactionCategoryGridComponent(
                    selectedCategory = parsed.category,
                    onCategoryChange = viewModel::updateParsedCategory,
                    transactionType = parsed.type
                )

                // Date & Time Cards
                TransactionDateTimeRowComponent(
                    dateText = parsed.dateText,
                    timeText = parsed.timeText,
                    onDateClick = { showDatePicker = true },
                    onTimeClick = { timePickerDialog.show() }
                )

                // Note/Catatan Input Card
                TransactionNoteCardComponent(
                    note = parsed.note,
                    onNoteChange = viewModel::updateParsedNote
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button (Green for income, Red/Accent for expense, White font color)
                val buttonColor = if (parsed.type == TransactionType.INCOME) Secondary else Accent
                val buttonOnColor = if (parsed.type == TransactionType.INCOME) OnSecondary else OnAccent

                Button(
                    onClick = { viewModel.saveTransaction() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = parsed.amount.isNotEmpty() && parsed.category.isNotEmpty() && !uiState.isSaving,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = buttonOnColor,
                        disabledContainerColor = BackgroundVariant,
                        disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = buttonOnColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan Transaksi",
                            style = MaterialTheme.typography.labelLarge.copy(color = Color.White)
                        )
                    }
                }
            }
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
                        viewModel.updateParsedDate(it)
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