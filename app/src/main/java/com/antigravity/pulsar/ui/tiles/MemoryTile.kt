package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
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
                    modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${state.usedPercentage.toInt()}%",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%.1f / %.1f GB".format(usedGb, totalGb),
                            style = MaterialTheme.typography.labelSmall,
                            color = PulsarPurple
                        )
                    }

                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "RAM Breakdown",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .width(88.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((usedGb / totalGb).coerceIn(0.05f, 0.95f))
                                    .background(PulsarPurple)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((availGb / totalGb).coerceIn(0.05f, 0.95f))
                                    .background(PulsarPurple.copy(alpha = 0.25f))
                            )
                        }
                        Text(
                            text = "ZRAM %.1fx Ratio".format(state.zramCompressionRatio),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "%.1f GB".format(availGb),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Available",
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
                        value = state.usedPercentage,
                        size = 85.dp,
                        strokeWidth = 8.dp,
                        arcColor = PulsarPurple,
                        subText = "RAM Used"
                    )
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text(
                            text = "Used: %.1f GB".format(usedGb),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PulsarPurple
                        )
                        Text(
                            text = "Free: %.1f GB".format(availGb),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total: %.1f GB".format(totalGb),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PulsarPurple.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ZRAM: %.1fx Ratio".format(state.zramCompressionRatio),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                color = PulsarPurple,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
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