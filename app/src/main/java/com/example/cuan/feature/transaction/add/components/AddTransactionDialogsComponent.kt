package com.example.cuan.feature.transaction.add.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cuan.core.utils.CurrencyUtils
import com.example.cuan.data.model.TransactionType
import com.example.cuan.ui.components.PrimaryButtonComponent
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.IncomeGreen
import com.example.cuan.ui.theme.TextSecondary

@Composable
fun AddTransactionDialogsComponent(
    showAutoSaveCountdown: Boolean,
    countdownSeconds: Int,
    onDismissAutoSave: () -> Unit,
    onConfirmAutoSave: () -> Unit,
    amount: String,
    category: String,
    note: String,
    transactionType: TransactionType
) {
    // Auto-Save Countdown Dialog
    if (showAutoSaveCountdown) {
        AlertDialog(
            onDismissRequest = onDismissAutoSave,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = IncomeGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Berhasil Membaca!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Detail transaksi yang diisi otomatis:")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BackgroundVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("• Nominal: Rp ${CurrencyUtils.formatRupiah(amount.toLongOrNull() ?: 0L)}")
                            Text("• Kategori: $category")
                            Text("• Catatan: ${note.ifBlank { "-" }}")
                            Text("• Jenis: ${if (transactionType == TransactionType.INCOME) "Pemasukan" else "Pengeluaran"}")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Transaksi disimpan otomatis dalam $countdownSeconds detik...",
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                }
            },
            confirmButton = {
                PrimaryButtonComponent(
                    text = "Simpan Sekarang",
                    onClick = onConfirmAutoSave
                )
            },
            dismissButton = {
                TextButton(onClick = onDismissAutoSave) {
                    Text("Batal & Edit", color = TextSecondary)
                }
            }
        )
    }
}
