package com.gothwad.internet.ui.components.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CanvasOverlayGrid() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val sizeVal = size.width
        val lineLength = 40.dp.toPx()
        val strokeWidth = 5.dp.toPx()
        val color = Color(0xFF1A73E8)

        // Top-Left corner
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(lineLength, strokeWidth)
        )
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(strokeWidth, lineLength)
        )

        // Top-Right corner
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(sizeVal - lineLength, 0f),
            size = androidx.compose.ui.geometry.Size(lineLength, strokeWidth)
        )
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(sizeVal - strokeWidth, 0f),
            size = androidx.compose.ui.geometry.Size(strokeWidth, lineLength)
        )

        // Bottom-Left corner
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(0f, sizeVal - strokeWidth),
            size = androidx.compose.ui.geometry.Size(lineLength, strokeWidth)
        )
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(0f, sizeVal - lineLength),
            size = androidx.compose.ui.geometry.Size(strokeWidth, lineLength)
        )

        // Bottom-Right corner
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(sizeVal - lineLength, sizeVal - strokeWidth),
            size = androidx.compose.ui.geometry.Size(lineLength, strokeWidth)
        )
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(sizeVal - strokeWidth, sizeVal - lineLength),
            size = androidx.compose.ui.geometry.Size(strokeWidth, lineLength)
        )
    }
}
