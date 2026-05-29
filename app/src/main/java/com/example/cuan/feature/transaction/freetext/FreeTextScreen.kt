package com.example.cuan.feature.transaction.freetext

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cuan.data.model.TransactionCategories
import com.example.cuan.data.model.TransactionType
import com.example.cuan.ui.components.CategoryChipComponent
import com.example.cuan.ui.components.PrimaryButtonComponent
import com.example.cuan.ui.components.SecondaryButtonComponent
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.SecondaryContainer
import com.example.cuan.ui.theme.TextSecondary
import com.example.cuan.ui.theme.SurfaceError
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.automirrored.filled.Message

// Free Text Input Screen (F-05) //
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FreeTextScreen(
    viewModel: FreeTextViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Input Teks",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                placeholder = { Text("Contoh: tadi beli kopi 35rb, atau terima gaji 5 juta", color = TextSecondary.copy(alpha = 0.6f)) },
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Secondary,
                    unfocusedBorderColor = BackgroundVariant,
                    focusedTextColor = TextSecondary,
                    unfocusedTextColor = TextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    }

    // Result Bottom Sheet
    if (uiState.showResultSheet && uiState.parsedData != null) {
        val parsed = uiState.parsedData!!

        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissResultSheet() },
            sheetState = sheetState,
            containerColor = Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Konfirmasi",
                    style = MaterialTheme.typography.headlineSmall
                )

                // Amount
                OutlinedTextField(
                    value = parsed.amount,
                    onValueChange = viewModel::updateParsedAmount,
                    label = { Text("Nominal") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = BackgroundVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Type Toggle
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = parsed.type == TransactionType.EXPENSE,
                        onClick = { viewModel.updateParsedType(TransactionType.EXPENSE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = SurfaceError,
                            activeContentColor = Accent
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingDown, null, modifier = Modifier.padding(end = 4.dp))
                        Text("Pengeluaran")
                    }
                    SegmentedButton(
                        selected = parsed.type == TransactionType.INCOME,
                        onClick = { viewModel.updateParsedType(TransactionType.INCOME) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = SecondaryContainer,
                            activeContentColor = Secondary
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, null, modifier = Modifier.padding(end = 4.dp))
                        Text("Pemasukan")
                    }
                }

                // Category
                Text("Kategori", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = TransactionCategories.getCategoriesForType(parsed.type)
                    categories.forEach { category ->
                        CategoryChipComponent(
                            label = category,
                            icon = getCategoryIcon(category),
                            isSelected = parsed.category == category,
                            onClick = { viewModel.updateParsedCategory(category) }
                        )
                    }
                }

                // Note
                OutlinedTextField(
                    value = parsed.note,
                    onValueChange = viewModel::updateParsedNote,
                    label = { Text("Catatan") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Secondary,
                        unfocusedBorderColor = BackgroundVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButtonComponent(
                        text = "Edit Lagi",
                        onClick = { viewModel.dismissResultSheet() },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButtonComponent(
                        text = "Simpan",
                        onClick = { viewModel.saveTransaction() },
                        icon = Icons.Default.CheckCircle,
                        enabled = parsed.amount.isNotEmpty() && parsed.category.isNotEmpty(),
                        isLoading = uiState.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Makan" -> Icons.Default.Fastfood
        "Transport" -> Icons.Default.DirectionsCar
        "Belanja" -> Icons.Default.ShoppingCart
        "Hiburan" -> Icons.Default.Movie
        "Kesehatan" -> Icons.Default.LocalHospital
        "Tagihan" -> Icons.AutoMirrored.Filled.ReceiptLong
        else -> Icons.Default.MoreHoriz
    }
}