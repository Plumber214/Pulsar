package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.MemoryState
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.theme.PulsarPurple
import com.antigravity.pulsar.ui.components.PulsarRadialGauge
import com.antigravity.pulsar.ui.components.PulsarTileContainer

@Composable
fun MemoryTile(
    config: TileConfig,
    state: MemoryState,
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
    val totalGb = state.totalRamBytes / (1024f * 1024f * 1024f)
    val usedGb = state.usedRamBytes / (1024f * 1024f * 1024f)
    val availGb = state.availableRamBytes / (1024f * 1024f * 1024f)

    PulsarTileContainer(
        config = config,
        title = "Memory",
        icon = Icons.Default.Storage,
        accentColor = PulsarPurple,
        badgeText = "RAM & ZRAM",
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
                    value = state.usedPercentage,
                    size = 75.dp,
                    strokeWidth = 7.dp,
                    arcColor = PulsarPurple,
                    subText = "RAM"
                )
            }
            TileSize.WIDE -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${state.usedPercentage.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Used: %.1f / %.1f GB".format(usedGb, totalGb),
                            style = MaterialTheme.typography.labelSmall,
                            color = PulsarPurple
                        )
                    }
                    Text(
                        text = "%.1f GB Free".format(availGb),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TileSize.STANDARD -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PulsarRadialGauge(
                        value = state.usedPercentage,
                        size = 85.dp,
                        strokeWidth = 8.dp,
                        arcColor = PulsarPurple,
                        subText = "RAM Used"
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Used: %.1f GB".format(usedGb),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarPurple
                        )
                        Text(
                            text = "Free: %.1f GB".format(availGb),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Total: %.1f GB".format(totalGb),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ZRAM: %.1fx Ratio".format(state.zramCompressionRatio),
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
                            text = "RAM: %.1f / %.1f GB (%d%%)".format(usedGb, totalGb, state.usedPercentage.toInt()),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarPurple
                        )
                        Text(
                            text = "Page Size: ${state.pageSizeKb} KB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Cached RAM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.1f GB".format(state.cachedRamBytes / (1024f * 1024f * 1024f)), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "ZRAM Swap Used", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.1f GB".format(state.usedSwapBytes / (1024f * 1024f * 1024f)), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "Compression", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "%.2fx".format(state.zramCompressionRatio), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PulsarPurple)
                        }
                    }
                }
            }
        }
    }
}