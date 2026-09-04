package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.BatteryState
import com.antigravity.pulsar.model.TemperatureUnit
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.theme.PulsarGreen
import com.antigravity.pulsar.ui.components.PulsarRadialGauge
import com.antigravity.pulsar.ui.components.PulsarTileContainer

@Composable
fun BatteryTile(
    config: TileConfig,
    state: BatteryState,
    tempUnit: TemperatureUnit,
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
    val tempDisplay = "${tempUnit.fromCelsius(state.temperatureCelsius).toInt()}${tempUnit.symbol}"

    PulsarTileContainer(
        config = config,
        title = "Battery",
        icon = Icons.Default.BatteryChargingFull,
        accentColor = PulsarGreen,
        badgeText = if (state.isCharging) "Charging • %.1f W".format(state.chargingWatts) else "Discharging • %.1f W".format(state.chargingWatts),
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
                PulsarRadialGauge(
                    value = state.levelPercentage.toFloat(),
                    size = 75.dp,
                    strokeWidth = 7.dp,
                    arcColor = PulsarGreen,
                    subText = if (state.isCharging) "Charging" else "Battery"
                )
            }
            TileSize.WIDE -> {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${state.levelPercentage}%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (state.isCharging) PulsarGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (state.isCharging) "CHG" else "BAT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = if (state.isCharging) PulsarGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "%.1f W".format(state.chargingWatts),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PulsarGreen
                        )
                        LinearProgressIndicator(
                            progress = { (state.chargingWatts.toFloat() / 30f).coerceIn(0.05f, 1f) },
                            modifier = Modifier
                                .width(64.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = PulsarGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        Text(
                            text = if (state.cycleCount != null && state.cycleCount > 0) "${state.cycleCount} cyc" else state.health,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = tempDisplay,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${state.voltageMv} mV",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            TileSize.STANDARD -> {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PulsarRadialGauge(
                        value = state.levelPercentage.toFloat(),
                        size = 85.dp,
                        strokeWidth = 8.dp,
                        arcColor = PulsarGreen,
                        subText = if (state.isCharging) "Charging" else "Level"
                    )
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "%.1f W".format(state.chargingWatts),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = PulsarGreen
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PulsarGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (state.isCharging) "+${state.chargingCurrentMa}mA" else "${state.chargingCurrentMa}mA",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                    color = PulsarGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Temp: $tempDisplay",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Voltage: ${state.voltageMv} mV",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (state.cycleCount != null && state.cycleCount > 0) "Health: ${state.health} • ${state.cycleCount} cyc" else "Health: ${state.health}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            TileSize.DETAILED -> {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level: ${state.levelPercentage}% • Temp: $tempDisplay",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarGreen
                        )
                        Text(
                            text = "Status: ${if (state.isCharging) "Charging" else "Discharging"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Power Draw", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.2f W".format(state.chargingWatts), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PulsarGreen)
                        }
                        Column {
                            Text(text = "Current", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${state.chargingCurrentMa} mA", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "Cycles", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = state.cycleCount?.toString() ?: "N/A", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "Capacity", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${state.capacityMah} mAh", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}