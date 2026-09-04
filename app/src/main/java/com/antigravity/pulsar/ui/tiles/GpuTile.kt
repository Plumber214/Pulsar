package com.antigravity.pulsar.ui.tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideogameAsset
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
import com.antigravity.pulsar.model.GpuState
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.ui.components.PulsarTileContainer

val PulsarMagenta = Color(0xFFE040FB)

@Composable
fun GpuTile(
    config: TileConfig,
    state: GpuState,
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
        title = "Graphics (GPU)",
        icon = Icons.Default.VideogameAsset,
        accentColor = PulsarMagenta,
        badgeText = state.vendor,
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
                    Text(
                        text = state.renderer.split(" ").takeLast(2).joinToString(" ").take(14),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = PulsarMagenta
                    )
                    Text(
                        text = state.vulkanVersion,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
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
                    Column(modifier = Modifier.weight(0.42f)) {
                        Text(
                            text = state.renderer,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                            color = PulsarMagenta,
                            maxLines = 1
                        )
                        Text(
                            text = state.vendor,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(
                        modifier = Modifier.weight(0.26f).padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PulsarMagenta.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Vulkan",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = PulsarMagenta,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${state.extensionCount} Exts",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(modifier = Modifier.weight(0.32f), horizontalAlignment = Alignment.End) {
                        Text(
                            text = state.vulkanVersion,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = state.glesVersion.take(14),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
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
                        Text(
                            text = state.renderer,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarMagenta
                        )
                        Text(
                            text = state.vulkanVersion,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Driver: ${state.glesVersion.take(24)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Vendor: ${state.vendor}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Hardware Extensions: ${state.extensionCount} active shaders & texture formats",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}