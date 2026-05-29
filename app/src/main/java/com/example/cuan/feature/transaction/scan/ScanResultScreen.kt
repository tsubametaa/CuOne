package com.example.cuan.feature.transaction.scan

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.Composable
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.MoreHoriz

/**
 * Scan Result Screen - Confirm parsed data from receipt (F-04)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ScanResultScreen(
    ocrText: String,
    viewModel: ScanResultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Process OCR text on first composition
    androidx.compose.runtime.LaunchedEffect(ocrText) {
        viewModel.parseOcrText(ocrText)
    }

    // Navigate on save success
    androidx.compose.runtime.LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Konfirmasi Hasil",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Parsed Result Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BackgroundVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Hasil Parse",
                        style = MaterialTheme.typography.labelLarge
                    )

                    // Amount
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = viewModel::updateAmount,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        label = { Text("Nominal") },
                        prefix = { Text("Rp ", color = TextSecondary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = BackgroundVariant,
                            focusedTextColor = TextSecondary,
                            unfocusedTextColor = TextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Type Toggle
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
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, null, modifier = Modifier.padding(end = 4.dp))
                            Text("Pengeluaran")
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
                        val categories = TransactionCategories.getCategoriesForType(uiState.type)
                        categories.forEach { category ->
                            CategoryChipComponent(
                                label = category,
                                icon = getCategoryIcon(category),
                                isSelected = uiState.category == category,
                                onClick = { viewModel.updateCategory(category) }
                            )
                        }
                    }

                    // Note
                    OutlinedTextField(
                        value = uiState.note,
                        onValueChange = viewModel::updateNote,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        label = { Text("Catatan") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = BackgroundVariant,
                            focusedTextColor = TextSecondary,
                            unfocusedTextColor = TextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Date
                    OutlinedTextField(
                        value = uiState.date,
                        onValueChange = { },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        label = { Text("Tanggal") },
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Secondary,
                            unfocusedBorderColor = BackgroundVariant,
                            focusedTextColor = TextSecondary,
                            unfocusedTextColor = TextSecondary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Warning if some fields not detected
            if (uiState.hasUndetectedFields) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceError),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingDown, // Using as warning icon
                            contentDescription = null,
                            tint = Accent
                        )
                        Text(
                            "Beberapa field tidak terdeteksi, lengkapi manually",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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