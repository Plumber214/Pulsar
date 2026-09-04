package com.antigravity.pulsar.ui.diagnostics

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.theme.PulsarBlue
import com.antigravity.pulsar.theme.PulsarGreen
import com.antigravity.pulsar.theme.PulsarMagenta
import com.antigravity.pulsar.theme.PulsarOrange
import com.antigravity.pulsar.theme.PulsarTeal
import com.antigravity.pulsar.theme.PulsarYellow

data class TouchPoint(val x: Float, val y: Float, val pressure: Float)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MultiTouchTestScreen(
    onComplete: (DiagnosticStatus) -> Unit,
    onBack: () -> Unit
) {
    val touchPoints = remember { mutableStateMapOf<Int, TouchPoint>() }
    var maxSimultaneous by remember { mutableIntStateOf(0) }

    val pointerColors = remember {
        listOf(
            PulsarTeal, PulsarGreen, PulsarBlue, PulsarMagenta,
            PulsarOrange, PulsarYellow, Color(0xFF00E5FF), Color(0xFFFF4081),
            Color(0xFF76FF03), Color(0xFFFFD600)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080B10))
            .pointerInteropFilter { event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_MOVE -> {
                        touchPoints.clear()
                        for (i in 0 until event.pointerCount) {
                            val id = event.getPointerId(i)
                            touchPoints[id] = TouchPoint(event.getX(i), event.getY(i), event.getPressure(i))
                        }
                        if (touchPoints.size > maxSimultaneous) {
                            maxSimultaneous = touchPoints.size
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                        val actionIndex = event.actionIndex
                        val id = event.getPointerId(actionIndex)
                        touchPoints.remove(id)
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        touchPoints.clear()
                    }
                }
                true
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            touchPoints.forEach { (id, pt) ->
                val color = pointerColors[id % pointerColors.size]
                // Outer glow ring
                drawCircle(
                    color = color.copy(alpha = 0.25f),
                    radius = 90f,
                    center = Offset(pt.x, pt.y)
                )
                // Middle ring
                drawCircle(
                    color = color,
                    radius = 50f,
                    center = Offset(pt.x, pt.y),
                    style = Stroke(width = 6f)
                )
                // Center point
                drawCircle(
                    color = Color.White,
                    radius = 12f,
                    center = Offset(pt.x, pt.y)
                )
                // Crosshairs
                drawLine(
                    color = color.copy(alpha = 0.6f),
                    start = Offset(pt.x - 70f, pt.y),
                    end = Offset(pt.x + 70f, pt.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = color.copy(alpha = 0.6f),
                    start = Offset(pt.x, pt.y - 70f),
                    end = Offset(pt.x, pt.y + 70f),
                    strokeWidth = 2f
                )
            }
        }

        // Top Controls Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xDD10141D),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Column {
                        Text(
                            text = "Multi-Touch Tracker",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Active: ${touchPoints.size} • Peak: $maxSimultaneous fingers",
                            style = MaterialTheme.typography.labelSmall,
                            color = PulsarTeal
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { onComplete(DiagnosticStatus.FAILED) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fail", tint = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { onComplete(DiagnosticStatus.PASSED) },
                        colors = ButtonDefaults.buttonColors(containerColor = PulsarGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Pass")
                        Text(text = "Pass", modifier = Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Bottom Coordinates Readout
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xAA000000)
        ) {
            Text(
                text = if (touchPoints.isEmpty()) "Touch the screen with multiple fingers to test digitizer" else touchPoints.entries.joinToString(" • ") { "#${it.key}: (%.0f, %.0f)".format(it.value.x, it.value.y) },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
