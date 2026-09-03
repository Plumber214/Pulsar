package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.TemperatureUnit
import com.antigravity.pulsar.model.ThermalState
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.theme.PulsarOrange
import com.antigravity.pulsar.theme.PulsarRed
import com.antigravity.pulsar.ui.components.PulsarRadialGauge
import com.antigravity.pulsar.ui.components.PulsarSparkline
import com.antigravity.pulsar.ui.components.PulsarTileContainer

@Composable
fun ThermalTile(
    config: TileConfig,
    state: ThermalState,
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
    val socDisplay = "${tempUnit.fromCelsius(state.socTempCelsius).toInt()}${tempUnit.symbol}"
    val batteryDisplay = "${tempUnit.fromCelsius(state.batteryTempCelsius).toInt()}${tempUnit.symbol}"
    val skinDisplay = "${tempUnit.fromCelsius(state.skinTempCelsius).toInt()}${tempUnit.symbol}"

    val accent = if (state.throttlingHeadroom >= 0.85f) PulsarRed else PulsarOrange

    PulsarTileContainer(
        config = config,
        title = "Thermals",
        icon = Icons.Default.Thermostat,
        accentColor = accent,
        badgeText = state.statusText,
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
                    value = (state.throttlingHeadroom * 100f).coerceIn(0f, 100f),
                    size = 75.dp,
                    strokeWidth = 7.dp,
                    arcColor = accent,
                    displayValueText = socDisplay,
                    subText = "SoC Temp"
                )
            }
            TileSize.WIDE -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(0.45f)) {
                        Text(
                            text = socDisplay,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "SoC • ${state.statusText}",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                    Box(modifier = Modifier.weight(0.55f).height(50.dp)) {
                        PulsarSparkline(
                            history = state.historyCelsius.map { tempUnit.fromCelsius(it) },
                            lineColor = accent
                        )
                    }
                }
            }
            TileSize.STANDARD -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PulsarRadialGauge(
                        value = (state.throttlingHeadroom * 100f).coerceIn(0f, 100f),
                        size = 85.dp,
                        strokeWidth = 8.dp,
                        arcColor = accent,
                        displayValueText = "${(state.throttlingHeadroom * 100).toInt()}%",
                        subText = "Headroom"
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Status: ${state.statusText}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accent
                        )
                        Text(
                            text = "SoC: $socDisplay",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Battery: $batteryDisplay",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Skin: $skinDisplay",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                            text = "Thermal Headroom: ${(state.throttlingHeadroom * 100).toInt()}% (${state.statusText})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = accent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "SoC Core", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = socDisplay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = accent)
                        }
                        Column {
                            Text(text = "Battery", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = batteryDisplay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "Chassis / Skin", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = skinDisplay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}