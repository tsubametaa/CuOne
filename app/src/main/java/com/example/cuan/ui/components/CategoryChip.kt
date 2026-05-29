package com.example.cuan.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.cuan.ui.theme.OnSecondary
import com.example.cuan.ui.theme.OnSecondaryContainer
import com.example.cuan.ui.theme.Secondary
import com.example.cuan.ui.theme.SecondaryContainer

/**
 * Category chip component for transaction form
 * Selected: background Secondary, text OnSecondary
 * Unselected: background SecondaryContainer, text OnSecondaryContainer
 */
@Composable
fun CategoryChipComponent(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Secondary else SecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) OnSecondary else OnSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) OnSecondary else OnSecondaryContainer
            )
        }
    }
}