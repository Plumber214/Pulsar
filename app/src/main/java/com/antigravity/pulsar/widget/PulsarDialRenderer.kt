package com.antigravity.pulsar.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object PulsarDialRenderer {

    // Default palette
    val COLOR_TRACK = Color.argb(80, 255, 255, 255) // Subtle semi-translucent track
    val COLOR_CONTAINER_BG = Color.rgb(15, 18, 24)   // Sleek dark container

    fun getCpuColor(loadPercent: Float): Int = when {
        loadPercent < 60f -> Color.rgb(0, 229, 255)   // Cyan
        loadPercent < 85f -> Color.rgb(255, 179, 0)   // Amber
        else -> Color.rgb(255, 23, 68)                // Red
    }

    fun getTempColor(tempC: Float): Int = when {
        tempC < 40f -> Color.rgb(0, 229, 255)         // Cool Cyan
        tempC < 50f -> Color.rgb(255, 179, 0)         // Warm Amber
        else -> Color.rgb(255, 23, 68)                // Hot Red
    }

    fun getBatteryColor(level: Int, isCharging: Boolean): Int = when {
        isCharging -> Color.rgb(0, 230, 118)          // Charging Green
        level <= 15 -> Color.rgb(255, 23, 68)         // Critical Red
        level <= 25 -> Color.rgb(255, 179, 0)         // Low Amber
        else -> Color.rgb(0, 230, 118)                // Healthy Green
    }

    fun getRamColor(usedPercent: Float): Int = when {
        usedPercent < 70f -> Color.rgb(124, 77, 255)  // Electric Violet
        usedPercent < 85f -> Color.rgb(179, 136, 255) // Light Violet
        else -> Color.rgb(255, 82, 82)                // Pressure Red
    }

    /**
     * Renders a high-resolution 270° radial dial gauge bitmap for Glance ImageProvider.
     */
    fun renderDialBitmap(
        progress: Float, // 0f to 1f
        arcColor: Int,
        trackColor: Int = COLOR_TRACK,
        sizePx: Int = 320,
        strokeWidthPx: Float = 24f,
        startAngle: Float = 135f,
        sweepAngle: Float = 270f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val halfStroke = strokeWidthPx / 2f
        val padding = halfStroke + 4f
        val rect = RectF(padding, padding, sizePx - padding, sizePx - padding)

        // Background Track
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.ROUND
            color = trackColor
        }
        canvas.drawArc(rect, startAngle, sweepAngle, false, trackPaint)

        // Active Value Arc
        val clampedProgress = progress.coerceIn(0.01f, 1f)
        val activeSweep = sweepAngle * clampedProgress
        val activePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.ROUND
            color = arcColor
        }
        canvas.drawArc(rect, startAngle, activeSweep, false, activePaint)

        return bitmap
    }
}
