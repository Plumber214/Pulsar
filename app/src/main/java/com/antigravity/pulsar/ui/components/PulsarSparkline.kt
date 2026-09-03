package com.antigravity.pulsar.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PulsarSparkline(
    history: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillAlpha: Float = 0.25f,
    maxBound: Float = 100f
) {
    if (history.isEmpty()) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val count = history.size

        if (count < 2) return@Canvas

        val maxVal = maxOf(maxBound, history.maxOrNull() ?: 100f)
        val stepX = width / (count - 1).toFloat()

        val linePath = Path()
        val fillPath = Path()

        for (i in 0 until count) {
            val x = i * stepX
            val ratio = (history[i] / maxVal).coerceIn(0f, 1f)
            val y = height - (ratio * height)

            if (i == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (i - 1) * stepX
                val prevRatio = (history[i - 1] / maxVal).coerceIn(0f, 1f)
                val prevY = height - (prevRatio * height)

                // Smooth cubic bezier
                val cX1 = prevX + (stepX / 2f)
                val cY1 = prevY
                val cX2 = prevX + (stepX / 2f)
                val cY2 = y

                linePath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = fillAlpha), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}