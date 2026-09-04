package com.antigravity.pulsar.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.pulsar.model.BatteryState
import com.antigravity.pulsar.model.CpuState
import com.antigravity.pulsar.model.DisplayState
import com.antigravity.pulsar.model.GpuState
import com.antigravity.pulsar.model.MemoryState
import com.antigravity.pulsar.model.NetworkState
import com.antigravity.pulsar.model.SensorsState
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
import com.antigravity.pulsar.ui.tiles.GpuTile
import com.antigravity.pulsar.ui.tiles.MemoryTile
import com.antigravity.pulsar.ui.tiles.NetworkTile
import com.antigravity.pulsar.ui.tiles.SensorsTile
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
    gpuState: GpuState,
    sensorsState: SensorsState,
    tempUnit: TemperatureUnit,
    speedUnit: SpeedUnit,
    isEditing: Boolean,
    onTileClick: (TileId) -> Unit,
    onResizeTile: (TileId, TileSize) -> Unit,
    onDeleteTile: (TileId) -> Unit,
    onMoveTile: (TileId, Int) -> Unit,
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState()
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(maxColumns),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = if (isEditing) 88.dp else 16.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = tiles,
            key = { _, item -> item.id.name },
            span = { _, item ->
                GridItemSpan(item.size.actualColSpan(maxColumns))
            }
        ) { index, config ->
            val baseHeight = when (config.size) {
                TileSize.MINI -> 140.dp
                TileSize.WIDE -> 140.dp
                TileSize.STANDARD -> 180.dp
                TileSize.DETAILED -> 255.dp
            }
            val tileHeight = baseHeight + if (isEditing) 35.dp else 0.dp

            val canMoveBack = index > 0
            val canMoveFwd = index < tiles.size - 1

            Box(
                modifier = Modifier
                    .animateItem()
                    .height(tileHeight)
            ) {
                when (config.id) {
                    TileId.CPU -> CpuTile(
                        config = config,
                        state = cpuState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.CPU) },
                        onResize = { onResizeTile(TileId.CPU, it) },
                        onDelete = { onDeleteTile(TileId.CPU) },
                        onMoveBackward = { onMoveTile(TileId.CPU, -1) },
                        onMoveForward = { onMoveTile(TileId.CPU, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                    TileId.BATTERY -> BatteryTile(
                        config = config,
                        state = batteryState,
                        tempUnit = tempUnit,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.BATTERY) },
                        onResize = { onResizeTile(TileId.BATTERY, it) },
                        onDelete = { onDeleteTile(TileId.BATTERY) },
                        onMoveBackward = { onMoveTile(TileId.BATTERY, -1) },
                        onMoveForward = { onMoveTile(TileId.BATTERY, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                    TileId.MEMORY -> MemoryTile(
                        config = config,
                        state = memoryState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.MEMORY) },
                        onResize = { onResizeTile(TileId.MEMORY, it) },
                        onDelete = { onDeleteTile(TileId.MEMORY) },
                        onMoveBackward = { onMoveTile(TileId.MEMORY, -1) },
                        onMoveForward = { onMoveTile(TileId.MEMORY, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                    TileId.THERMAL -> ThermalTile(
                        config = config,
                        state = thermalState,
                        tempUnit = tempUnit,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.THERMAL) },
                        onResize = { onResizeTile(TileId.THERMAL, it) },
                        onDelete = { onDeleteTile(TileId.THERMAL) },
                        onMoveBackward = { onMoveTile(TileId.THERMAL, -1) },
                        onMoveForward = { onMoveTile(TileId.THERMAL, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                    TileId.NETWORK -> NetworkTile(
                        config = config,
                        state = networkState,
                        speedUnit = speedUnit,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.NETWORK) },
                        onResize = { onResizeTile(TileId.NETWORK, it) },
                        onDelete = { onDeleteTile(TileId.NETWORK) },
                        onMoveBackward = { onMoveTile(TileId.NETWORK, -1) },
                        onMoveForward = { onMoveTile(TileId.NETWORK, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                    TileId.STORAGE -> StorageTile(
                        config = config,
                        state = storageState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.STORAGE) },
                        onResize = { onResizeTile(TileId.STORAGE, it) },
                        onDelete = { onDeleteTile(TileId.STORAGE) },
                        onMoveBackward = { onMoveTile(TileId.STORAGE, -1) },
                        onMoveForward = { onMoveTile(TileId.STORAGE, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                    TileId.DISPLAY -> DisplayTile(
                        config = config,
                        state = displayState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.DISPLAY) },
                        onResize = { onResizeTile(TileId.DISPLAY, it) },
                        onDelete = { onDeleteTile(TileId.DISPLAY) },
                        onMoveBackward = { onMoveTile(TileId.DISPLAY, -1) },
                        onMoveForward = { onMoveTile(TileId.DISPLAY, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                    TileId.GPU -> GpuTile(
                        config = config,
                        state = gpuState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.GPU) },
                        onResize = { onResizeTile(TileId.GPU, it) },
                        onDelete = { onDeleteTile(TileId.GPU) },
                        onMoveBackward = { onMoveTile(TileId.GPU, -1) },
                        onMoveForward = { onMoveTile(TileId.GPU, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                    TileId.SENSORS -> SensorsTile(
                        config = config,
                        state = sensorsState,
                        isEditing = isEditing,
                        onTileClick = { onTileClick(TileId.SENSORS) },
                        onResize = { onResizeTile(TileId.SENSORS, it) },
                        onDelete = { onDeleteTile(TileId.SENSORS) },
                        onMoveBackward = { onMoveTile(TileId.SENSORS, -1) },
                        onMoveForward = { onMoveTile(TileId.SENSORS, 1) },
                        canMoveBackward = canMoveBack,
                        canMoveForward = canMoveFwd
                    )
                }
            }
        }
    }
}