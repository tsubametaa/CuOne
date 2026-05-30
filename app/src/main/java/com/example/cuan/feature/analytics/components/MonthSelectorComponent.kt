package com.example.cuan.feature.analytics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cuan.core.utils.DateUtils
import com.example.cuan.ui.theme.BackgroundVariant
import com.example.cuan.ui.theme.OnBackground
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.Secondary
import java.time.YearMonth

// Modern, flat month selection component 
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthSelectorComponent(
    months: List<YearMonth>,
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(months) { month ->
            val isSelected = month == selectedMonth
            FilterChip(
                selected = isSelected,
                onClick = { onMonthSelected(month) },
                label = { 
                    Text(
                        text = DateUtils.formatShortMonth(month),
                        color = if (isSelected) OnSecondary else OnBackground
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Secondary,
                    selectedLabelColor = OnSecondary,
                    containerColor = BackgroundVariant,
                    labelColor = OnBackground
                ),
                border = null
            )
        }
    }
}
