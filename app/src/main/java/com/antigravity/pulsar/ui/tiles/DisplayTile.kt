package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.antigravity.pulsar.model.DisplayState
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.theme.PulsarTeal
import com.antigravity.pulsar.ui.components.PulsarRadialGauge
import com.antigravity.pulsar.ui.components.PulsarTileContainer

@Composable
fun DisplayTile(
    config: TileConfig,
    state: DisplayState,
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
        title = "Display",
        icon = Icons.Default.Tv,
        accentColor = PulsarTeal,
        badgeText = "${state.widthPx}×${state.heightPx}",
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
                    value = (state.refreshRateHz / 120f * 100f).coerceIn(0f, 100f),
                    size = 75.dp,
                    strokeWidth = 7.dp,
                    arcColor = PulsarTeal,
                    displayValueText = "${state.refreshRateHz.toInt()}",
                    subText = "Hz"
                )
            }
            TileSize.WIDE, TileSize.STANDARD, TileSize.DETAILED -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PulsarRadialGauge(
                        value = (state.refreshRateHz / 120f * 100f).coerceIn(0f, 100f),
                        size = 80.dp,
                        strokeWidth = 8.dp,
                        arcColor = PulsarTeal,
                        displayValueText = "${state.refreshRateHz.toInt()}",
                        subText = "Hz"
                    )
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Rate: ${state.refreshRateHz.toInt()} Hz",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarTeal
                        )
                        Text(
                            text = "Res: ${state.widthPx}×${state.heightPx}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Density: ${state.densityDpi} DPI",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (state.isHdrSupported) "HDR: Supported" else "HDR: Standard",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}