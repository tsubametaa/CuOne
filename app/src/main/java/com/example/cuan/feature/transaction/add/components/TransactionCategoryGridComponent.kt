package com.example.cuan.feature.transaction.add.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cuan.data.model.TransactionCategories
import com.example.cuan.data.model.TransactionType
import com.example.cuan.ui.theme.Accent
import com.example.cuan.ui.theme.Background
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.SecondaryContainer
import com.example.cuan.ui.theme.SurfaceError
import com.example.cuan.ui.theme.TextSecondary

@Composable
fun TransactionCategoryGridComponent(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    transactionType: TransactionType,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BackgroundVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "PILIH KATEGORI",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            
            val categories = TransactionCategories.getCategoriesForType(transactionType)
            val icons = getCategoryIcons()
            val activeColor = if (transactionType == TransactionType.INCOME) Secondary else Accent
            val activeContainer = if (transactionType == TransactionType.INCOME) SecondaryContainer else SurfaceError
            
            val chunkedCategories = categories.chunked(3)
            chunkedCategories.forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    rowCategories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onCategoryChange(category) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = if (isSelected) activeContainer else Background,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) activeColor else BackgroundVariant,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icons[category] ?: Icons.Default.Receipt,
                                    contentDescription = category,
                                    tint = if (isSelected) activeColor else OnBackground,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) activeColor else OnBackground
                            )
                        }
                    }
                    
                    if (rowCategories.size < 3) {
                        repeat(3 - rowCategories.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private fun getCategoryIcons(): Map<String, ImageVector> = mapOf(
    "Makan" to Icons.Default.Fastfood,
    "Transport" to Icons.Default.DirectionsCar,
    "Belanja" to Icons.Default.ShoppingCart,
    "Kesehatan" to Icons.Default.LocalHospital,
    "Pendidikan" to Icons.Default.School,
    "Hiburan" to Icons.Default.LocalActivity,
    "Tagihan" to Icons.Default.Lightbulb,
    "Rumah" to Icons.Default.Home,
    "Lainnya" to Icons.Default.MoreHoriz,
    
    // Income Categories (Professional Redesign)
    "Gaji" to Icons.Default.MonetizationOn,
    "Freelance" to Icons.Default.Computer,
    "Bisnis" to Icons.Default.Store,
    "Investasi" to Icons.AutoMirrored.Filled.ShowChart,
    "Hadiah" to Icons.Default.CardGiftcard
)
