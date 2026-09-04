package com.antigravity.pulsar.ui.diagnostics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.SensorsState
import com.antigravity.pulsar.theme.PulsarGreen
import com.antigravity.pulsar.theme.PulsarOrange
import com.antigravity.pulsar.theme.PulsarTeal
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsToolScreen(
    state: SensorsState,
    onComplete: (DiagnosticStatus) -> Unit,
    onBack: () -> Unit
) {
    // Pitch & Roll Calculation
    val norm = sqrt(state.accelX * state.accelX + state.accelY * state.accelY + state.accelZ * state.accelZ)
    val pitchDeg = if (norm > 0) Math.toDegrees(atan2(-state.accelX.toDouble(), sqrt(state.accelY * state.accelY + state.accelZ * state.accelZ).toDouble())) else 0.0
    val rollDeg = if (norm > 0) Math.toDegrees(atan2(state.accelY.toDouble(), state.accelZ.toDouble())) else 0.0

    val isLevel = Math.abs(pitchDeg) < 0.8 && Math.abs(rollDeg) < 0.8
    val levelAccent = if (isLevel) PulsarGreen else PulsarTeal

    // Barometric Elevation (Standard Hypsometric Formula)
    val pressure = state.pressureHpa
    val altitudeMeters = if (pressure != null) {
        44330.0 * (1.0 - (pressure / 1013.25).pow(0.190294957))
    } else null
    val altitudeFeet = altitudeMeters?.times(3.28084)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Sensors & Spirit Level", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onComplete(DiagnosticStatus.FAILED) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fail", tint = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { onComplete(DiagnosticStatus.PASSED) },
                        colors = ButtonDefaults.buttonColors(containerColor = PulsarGreen, contentColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Pass")
                        Text(text = "Pass", modifier = Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Spirit Bubble Level Target
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isLevel) "LEVEL (0° CALIBRATED)" else "3-AXIS SPIRIT LEVEL",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                        color = levelAccent
                    )

                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .background(Color(0xFF0C1017), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val maxRadius = size.width / 2 - 10f

                            // Outer border
                            drawCircle(color = levelAccent.copy(alpha = 0.3f), radius = maxRadius, center = center, style = Stroke(width = 3f))
                            // Intermediate circles
                            drawCircle(color = Color.DarkGray, radius = maxRadius * 0.66f, center = center, style = Stroke(width = 2f))
                            drawCircle(color = Color.DarkGray, radius = maxRadius * 0.33f, center = center, style = Stroke(width = 2f))
                            // Center target
                            drawCircle(color = levelAccent, radius = 18f, center = center, style = Stroke(width = 3f))

                            // Crosshairs
                            drawLine(color = Color(0x44FFFFFF), start = Offset(center.x, 10f), end = Offset(center.x, size.height - 10f), strokeWidth = 1.5f)
                            drawLine(color = Color(0x44FFFFFF), start = Offset(10f, center.y), end = Offset(size.width - 10f, center.y), strokeWidth = 1.5f)

                            // Moving Bubble position
                            val bubbleX = (center.x + (rollDeg.toFloat() / 20f) * maxRadius).coerceIn(20f, size.width - 20f)
                            val bubbleY = (center.y + (pitchDeg.toFloat() / 20f) * maxRadius).coerceIn(20f, size.height - 20f)

                            drawCircle(
                                color = levelAccent.copy(alpha = 0.35f),
                                radius = 28f,
                                center = Offset(bubbleX, bubbleY)
                            )
                            drawCircle(
                                color = levelAccent,
                                radius = 20f,
                                center = Offset(bubbleX, bubbleY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 6f,
                                center = Offset(bubbleX, bubbleY)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Pitch", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.1f°".format(pitchDeg), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = levelAccent)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Roll", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.1f°".format(rollDeg), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = levelAccent)
                        }
                    }
                }
            }

            // Barometric Altimeter Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Barometric Altimeter & Pressure",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = PulsarOrange
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Atmospheric Pressure", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = if (pressure != null) "%.2f hPa".format(pressure) else "Sensor Unavailable", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Calculated Elevation (MSL)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (altitudeMeters != null) "%.0f m (%.0f ft)".format(altitudeMeters, altitudeFeet) else "--",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarOrange
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Ambient Illuminance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = if (state.lightLux != null) "${state.lightLux.toInt()} Lux" else "N/A", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
