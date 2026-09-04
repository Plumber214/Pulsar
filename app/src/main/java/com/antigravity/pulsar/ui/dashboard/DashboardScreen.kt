package com.antigravity.pulsar.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.theme.PulsarTeal
import com.antigravity.pulsar.ui.navigation.SettingsDialog
import com.antigravity.pulsar.ui.specs.SpecsBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    maxColumns: Int,
    modifier: Modifier = Modifier
) {
    val tiles by viewModel.tiles.collectAsState()
    val prefs by viewModel.preferences.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val isAddTileOpen by viewModel.isAddTileOpen.collectAsState()
    val selectedTileForSpecs by viewModel.selectedTileForSpecs.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()

    val cpuState by viewModel.telemetryRepo.cpuState.collectAsState()
    val memoryState by viewModel.telemetryRepo.memoryState.collectAsState()
    val batteryState by viewModel.telemetryRepo.batteryState.collectAsState()
    val thermalState by viewModel.telemetryRepo.thermalState.collectAsState()
    val networkState by viewModel.telemetryRepo.networkState.collectAsState()
    val storageState by viewModel.telemetryRepo.storageState.collectAsState()
    val displayState by viewModel.telemetryRepo.displayState.collectAsState()
    val gpuState by viewModel.gpuState.collectAsState()
    val sensorsState by viewModel.sensorsState.collectAsState()
    val specs by viewModel.telemetryRepo.deviceSpecs.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val isHudActive by com.antigravity.pulsar.service.hud.PulsarHudService.isHudActive.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Pulsar",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = specs.phoneModel,
                            style = MaterialTheme.typography.labelSmall,
                            color = PulsarTeal
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { com.antigravity.pulsar.service.hud.PulsarHudService.toggle(context) }) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle Floating HUD",
                            tint = if (isHudActive) PulsarTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.openAddTile() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Tile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.toggleEditing() }) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Done Editing" else "Edit Dashboard",
                            tint = if (isEditing) PulsarTeal else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { viewModel.setSettingsOpen(true) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BentoGridLayout(
                tiles = tiles,
                maxColumns = maxColumns,
                cpuState = cpuState,
                memoryState = memoryState,
                batteryState = batteryState,
                thermalState = thermalState,
                networkState = networkState,
                storageState = storageState,
                displayState = displayState,
                gpuState = gpuState,
                sensorsState = sensorsState,
                tempUnit = prefs.temperatureUnit,
                speedUnit = prefs.speedUnit,
                isEditing = isEditing,
                onTileClick = { viewModel.openSpecs(it) },
                onResizeTile = { id, size -> viewModel.resizeTile(id, size) },
                onDeleteTile = { viewModel.deleteTile(it) },
                onMoveTile = { id, dir -> viewModel.moveTile(id, dir) },
                gridState = viewModel.gridState
            )

            if (isEditing) {
                EditModeBar(
                    onAddTile = { viewModel.openAddTile() },
                    onUndo = { viewModel.undo() },
                    canUndo = canUndo,
                    onDone = { viewModel.setEditing(false) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    if (isAddTileOpen) {
        val availableTiles by viewModel.availableTilesToAdd.collectAsState()
        AddTileBottomSheet(
            availableTiles = availableTiles,
            onAddTile = { viewModel.addTile(it) },
            onDismiss = { viewModel.setAddTileOpen(false) }
        )
    }

    if (selectedTileForSpecs != null) {
        SpecsBottomSheet(
            specs = specs,
            cpuState = cpuState,
            memoryState = memoryState,
            batteryState = batteryState,
            displayState = displayState,
            gpuState = gpuState,
            sensorsState = sensorsState,
            initialFocusTile = selectedTileForSpecs,
            onDismiss = { viewModel.closeSpecs() }
        )
    }

    if (isSettingsOpen) {
        SettingsDialog(
            preferences = prefs,
            onTempUnitChange = { viewModel.updateTemperatureUnit(it) },
            onSpeedUnitChange = { viewModel.updateSpeedUnit(it) },
            onAmoledChange = { viewModel.updateAmoled(it) },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }
}