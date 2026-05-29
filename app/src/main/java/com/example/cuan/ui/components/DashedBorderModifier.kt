package com.example.cuan.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp

/**
 * Extension modifier to draw a dashed border around compose elements.
 */
fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp,
    dashLength: Dp,
    gapLength: Dp,
    cornerRadius: Dp
): Modifier = this.drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val dashLengthPx = dashLength.toPx()
    val gapLengthPx = gapLength.toPx()
    val cornerRadiusPx = cornerRadius.toPx()
    
    val paint = Paint().apply {
        this.color = color
        this.style = PaintingStyle.Stroke
        this.strokeWidth = strokeWidthPx
        this.pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLengthPx, gapLengthPx),
            0f
        )
    }
    
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                rect = Rect(
                    offset = Offset(strokeWidthPx/2, strokeWidthPx/2),
                    size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
                ),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
        )
    }
    
    drawContext.canvas.drawPath(path, paint)
}
