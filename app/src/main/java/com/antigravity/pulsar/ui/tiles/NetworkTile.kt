package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.NetworkState
import com.antigravity.pulsar.model.SpeedUnit
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.theme.PulsarBlue
import com.antigravity.pulsar.ui.components.PulsarRadialGauge
import com.antigravity.pulsar.ui.components.PulsarSparkline
import com.antigravity.pulsar.ui.components.PulsarTileContainer

@Composable
fun NetworkTile(
    config: TileConfig,
    state: NetworkState,
    speedUnit: SpeedUnit,
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
    val factor = if (speedUnit == SpeedUnit.BITS_PER_SEC) 8f else 1f
    val unitSuffix = speedUnit.displayName

    val rxValue = (state.downloadBytesPerSec * factor) / (1024f * 1024f)
    val txValue = (state.uploadBytesPerSec * factor) / (1024f * 1024f)

    val rxText = if (rxValue >= 10f) "%.1f %s".format(rxValue, unitSuffix) else "%.2f %s".format(rxValue, unitSuffix)
    val txText = if (txValue >= 10f) "%.1f %s".format(txValue, unitSuffix) else "%.2f %s".format(txValue, unitSuffix)

    PulsarTileContainer(
        config = config,
        title = "Network",
        icon = Icons.Default.Wifi,
        accentColor = PulsarBlue,
        badgeText = if (state.isWifiConnected) state.wifiSsid else state.cellularNetworkType,
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
                    value = (rxValue * 10f).coerceIn(0f, 100f),
                    size = 75.dp,
                    strokeWidth = 7.dp,
                    arcColor = PulsarBlue,
                    displayValueText = "%.1f".format(rxValue),
                    subText = unitSuffix
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
                            text = "↓ $rxText",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PulsarBlue
                        )
                        Text(
                            text = "↑ $txText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(modifier = Modifier.weight(0.55f).height(50.dp)) {
                        PulsarSparkline(
                            history = state.downloadHistoryMb,
                            lineColor = PulsarBlue
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
                        value = (rxValue * 10f).coerceIn(0f, 100f),
                        size = 85.dp,
                        strokeWidth = 8.dp,
                        arcColor = PulsarBlue,
                        displayValueText = "%.1f".format(rxValue),
                        subText = "Down $unitSuffix"
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Down: $rxText",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarBlue
                        )
                        Text(
                            text = "Up: $txText",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (state.isWifiConnected) "Link: ${state.wifiLinkSpeedMbps} Mbps" else "Cell: ${state.cellularNetworkType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (state.wifiFrequencyGhz > 0f) {
                            Text(
                                text = "Band: ${state.wifiFrequencyGhz} GHz",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
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
                            text = "Download: $rxText • Upload: $txText",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarBlue
                        )
                        Text(
                            text = if (state.isWifiConnected) "Wi-Fi (${state.wifiFrequencyGhz} GHz)" else "Cellular",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "SSID / Network", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = state.wifiSsid, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "Link Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${state.wifiLinkSpeedMbps} Mbps", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PulsarBlue)
                        }
                        Column {
                            Text(text = "Cell State", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = state.cellularNetworkType, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}