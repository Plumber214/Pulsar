package com.antigravity.pulsar.service.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.BatteryState
import com.antigravity.pulsar.model.CpuState
import com.antigravity.pulsar.model.MemoryState
import com.antigravity.pulsar.model.TemperatureUnit
import com.antigravity.pulsar.model.ThermalState
import com.antigravity.pulsar.theme.PulsarGreen
import com.antigravity.pulsar.theme.PulsarOrange
import com.antigravity.pulsar.theme.PulsarPurple
import com.antigravity.pulsar.theme.PulsarTeal

@Composable
fun FloatingHudContent(
    fps: Int,
    cpuState: CpuState,
    memoryState: MemoryState,
    batteryState: BatteryState,
    thermalState: ThermalState,
    tempUnit: TemperatureUnit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onDragEnd: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val socTempDisplay = "${tempUnit.fromCelsius(thermalState.socTempCelsius).toInt()}${tempUnit.symbol}"

    val dragModifier = Modifier.pointerInput(Unit) {
        detectDragGestures(
            onDragEnd = { onDragEnd() },
            onDrag = { change, dragAmount ->
                change.consume()
                onDrag(dragAmount.x, dragAmount.y)
            }
        )
    }

    Box(modifier = modifier) {
        if (!isExpanded) {
            // Collapsed Micro-Pill Mode
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable { isExpanded = true },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xEE0D1117),
                border = BorderStroke(1.dp, PulsarTeal.copy(alpha = 0.5f)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .then(dragModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragIndicator,
                            contentDescription = "Drag HUD",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // FPS Pill
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PulsarGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "$fps FPS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                            color = PulsarGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // CPU Load
                    Text(
                        text = "CPU ${cpuState.overallLoad.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = PulsarTeal
                    )

                    // Battery / Watts
                    Text(
                        text = "%.1fW".format(batteryState.chargingWatts),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                        color = PulsarOrange
                    )

                    // Temp
                    Text(
                        text = socTempDisplay,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.LightGray
                    )

                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand HUD",
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        } else {
            // Expanded Card Mode
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xF20F141E),
                border = BorderStroke(1.2.dp, PulsarTeal.copy(alpha = 0.7f)),
                shadowElevation = 12.dp,
                modifier = Modifier
                    .width(260.dp)
                    .heightIn(max = 290.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .then(dragModifier),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PULSAR HUD",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                                color = PulsarTeal
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PulsarGreen.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "$fps FPS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = PulsarGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Minimize",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close HUD",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // CPU Section
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "CPU Load", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "${cpuState.overallLoad.toInt()}% (${cpuState.currentAvgFreqMhz}MHz)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PulsarTeal)
                        }
                        LinearProgressIndicator(
                            progress = { (cpuState.overallLoad / 100f).coerceIn(0.05f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PulsarTeal,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }

                    // RAM Section
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        val usedGb = memoryState.usedRamBytes / (1024f * 1024f * 1024f)
                        val totalGb = memoryState.totalRamBytes / (1024f * 1024f * 1024f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "RAM Usage", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "%.1f / %.1f GB (%d%%)".format(usedGb, totalGb, memoryState.usedPercentage.toInt()), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PulsarPurple)
                        }
                        LinearProgressIndicator(
                            progress = { (memoryState.usedPercentage / 100f).coerceIn(0.05f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PulsarPurple,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }

                    // Battery & Thermals Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Battery", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            val wattsFormatted = "%.1f".format(batteryState.chargingWatts)
                            Text(
                                text = "${batteryState.levelPercentage}% (${wattsFormatted}W)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PulsarGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "SoC Temp", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = socTempDisplay,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PulsarOrange
                            )
                        }
                    }
                }
            }
        }
    }
}
