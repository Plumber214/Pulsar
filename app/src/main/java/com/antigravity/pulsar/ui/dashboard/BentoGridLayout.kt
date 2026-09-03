package com.antigravity.pulsar.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.pulsar.model.BatteryState
import com.antigravity.pulsar.model.CpuState
import com.antigravity.pulsar.model.DisplayState
import com.antigravity.pulsar.model.MemoryState
import com.antigravity.pulsar.model.NetworkState
import com.antigravity.pulsar.model.SpeedUnit
import com.antigravity.pulsar.model.StorageState
import com.antigravity.pulsar.model.TemperatureUnit
import com.antigravity.pulsar.model.ThermalState
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileId
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.ui.tiles.BatteryTile
import com.antigravity.pulsar.ui.tiles.CpuTile
import com.antigravity.pulsar.ui.tiles.DisplayTile
import com.antigravity.pulsar.ui.tiles.MemoryTile
import com.antigravity.pulsar.ui.tiles.NetworkTile
import com.antigravity.pulsar.ui.tiles.StorageTile
import com.antigravity.pulsar.ui.tiles.ThermalTile

@Composable
fun BentoGridLayout(
    tiles: List<TileConfig>,
    maxColumns: Int,
    cpuState: CpuState,
    memoryState: MemoryState,
    batteryState: BatteryState,
    thermalState: ThermalState,
    networkState: NetworkState,
    storageState: StorageState,
    displayState: DisplayState,
    tempUnit: TemperatureUnit,
    speedUnit: SpeedUnit,
    isEditing: Boolean,
    onTileClick: (TileId) -> Unit,
    onResizeTile: (TileId, TileSize) -> Unit,
    onDeleteTile: (TileId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(maxColumns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = tiles,
            key = { it.id.name },
            span = { item ->
                GridItemSpan(item.size.actualColSpan(maxColumns))
            }
        ) { config ->
            val tileHeight = when (config.size) {
                TileSize.MINI -> 135.dp
                TileSize.WIDE -> 125.dp
                TileSize.STANDARD -> 175.dp
                TileSize.DETAILED -> 250.dp
            }

            Box(modifier = Modifier.height(tileHeight)) {
                when (config.id) {
                    TileId.CPU -> CpuTile(
                        config = config,
                        state = cpuState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.CPU) },
                        onResize = { onResizeTile(TileId.CPU, it) },
                        onDelete = { onDeleteTile(TileId.CPU) }
                    )
                    TileId.BATTERY -> BatteryTile(
                        config = config,
                        state = batteryState,
                        tempUnit = tempUnit,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.BATTERY) },
                        onResize = { onResizeTile(TileId.BATTERY, it) },
                        onDelete = { onDeleteTile(TileId.BATTERY) }
                    )
                    TileId.MEMORY -> MemoryTile(
                        config = config,
                        state = memoryState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.MEMORY) },
                        onResize = { onResizeTile(TileId.MEMORY, it) },
                        onDelete = { onDeleteTile(TileId.MEMORY) }
                    )
                    TileId.THERMAL -> ThermalTile(
                        config = config,
                        state = thermalState,
                        tempUnit = tempUnit,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.THERMAL) },
                        onResize = { onResizeTile(TileId.THERMAL, it) },
                        onDelete = { onDeleteTile(TileId.THERMAL) }
                    )
                    TileId.NETWORK -> NetworkTile(
                        config = config,
                        state = networkState,
                        speedUnit = speedUnit,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.NETWORK) },
                        onResize = { onResizeTile(TileId.NETWORK, it) },
                        onDelete = { onDeleteTile(TileId.NETWORK) }
                    )
                    TileId.STORAGE -> StorageTile(
                        config = config,
                        state = storageState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.STORAGE) },
                        onResize = { onResizeTile(TileId.STORAGE, it) },
                        onDelete = { onDeleteTile(TileId.STORAGE) }
                    )
                    TileId.DISPLAY -> DisplayTile(
                        config = config,
                        state = displayState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.DISPLAY) },
                        onResize = { onResizeTile(TileId.DISPLAY, it) },
                        onDelete = { onDeleteTile(TileId.DISPLAY) }
                    )
                }
            }
        }
    }
}