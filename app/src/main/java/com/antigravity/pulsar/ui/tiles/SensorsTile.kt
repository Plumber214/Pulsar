package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.SensorsState
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.ui.components.PulsarTileContainer

val PulsarAmberLight = Color(0xFFFFB74D)

@Composable
fun SensorsTile(
    config: TileConfig,
    state: SensorsState,
    isEditing: Boolean,
    onTileClick: () -> Unit,
    onResize: (TileSize) -> Unit,
    onDelete: () -> Unit,
    onMoveBackward: (() -> Unit)? = null,
    onMoveForward: (() -> Unit)? = null,
    canMoveBackward: Boolean = false,
    canMoveForward: Boolean = false,
    modifier: Modifier = Modifier
) {
    PulsarTileContainer(
        config = config,
        title = "Sensors",
        icon = Icons.Default.Sensors,
        accentColor = PulsarAmberLight,
        badgeText = "${state.totalSensorsCount} Active",
        isEditing = isEditing,
        onTileClick = onTileClick,
        onResize = onResize,
        onDelete = onDelete,
        onMoveBackward = onMoveBackward,
        onMoveForward = onMoveForward,
        canMoveBackward = canMoveBackward,
        canMoveForward = canMoveForward,
        modifier = modifier
    ) {
        when (config.size) {
            TileSize.MINI -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val p = state.pressureHpa
                    Text(
                        text = if (p != null) "%.1f".format(p) else "--",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color = PulsarAmberLight
                    )
                    Text(
                        text = "hPa",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TileSize.WIDE -> {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        val p = state.pressureHpa
                        Text(
                            text = if (p != null) "%.1f hPa".format(p) else "N/A",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = PulsarAmberLight
                        )
                        Text(
                            text = "Barometer",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PulsarAmberLight.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "X:%.0f Y:%.0f Z:%.0f".format(state.accelX, state.accelY, state.accelZ),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = PulsarAmberLight,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "3-Axis Tilt",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val lux = state.lightLux
                        Text(
                            text = if (lux != null) "${lux.toInt()} Lux" else "N/A",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ambient Light",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            TileSize.STANDARD, TileSize.DETAILED -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val p = state.pressureHpa
                            Text(
                                text = if (p != null) "%.1f hPa".format(p) else "N/A",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PulsarAmberLight
                            )
                            Text(
                                text = "Barometer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            val lux = state.lightLux
                            Text(
                                text = if (lux != null) "${lux.toInt()} Lux" else "N/A",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ambient Light",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tilt: X:%.1f Y:%.1f Z:%.1f".format(state.accelX, state.accelY, state.accelZ),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = PulsarAmberLight
                            )
                            Text(
                                text = "Accelerometer (m/s²)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${state.totalSensorsCount} Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}