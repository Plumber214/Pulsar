package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antigravity.pulsar.model.StorageState
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.theme.PulsarYellow
import com.antigravity.pulsar.ui.components.PulsarRadialGauge
import com.antigravity.pulsar.ui.components.PulsarTileContainer

@Composable
fun StorageTile(
    config: TileConfig,
    state: StorageState,
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
    val totalGb = state.totalBytes / (1024f * 1024f * 1024f)
    val usedGb = state.usedBytes / (1024f * 1024f * 1024f)
    val freeGb = state.freeBytes / (1024f * 1024f * 1024f)

    PulsarTileContainer(
        config = config,
        title = "Storage",
        icon = Icons.Default.Folder,
        accentColor = PulsarYellow,
        badgeText = "Internal",
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
                    arcColor = PulsarYellow,
                    subText = "Used"
                )
            }
            TileSize.WIDE, TileSize.STANDARD, TileSize.DETAILED -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PulsarRadialGauge(
                        value = state.usedPercentage,
                        size = 80.dp,
                        strokeWidth = 8.dp,
                        arcColor = PulsarYellow,
                        subText = "Storage"
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Used: %.1f GB".format(usedGb),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarYellow
                        )
                        Text(
                            text = "Free: %.1f GB".format(freeGb),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total: %.1f GB".format(totalGb),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}