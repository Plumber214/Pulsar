package com.antigravity.pulsar.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.antigravity.pulsar.data.TelemetryRepository
import com.antigravity.pulsar.data.preferences.UserPreferencesRepository
import com.antigravity.pulsar.model.SpeedUnit
import com.antigravity.pulsar.model.TemperatureUnit
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileId
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    val telemetryRepo = TelemetryRepository(application)
    val preferencesRepo = UserPreferencesRepository(application)

    val preferences: StateFlow<UserPreferences> = preferencesRepo.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val tiles: StateFlow<List<TileConfig>> = preferencesRepo.dashboardTilesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, preferencesRepo.defaultDashboardTiles())

    val availableTilesToAdd: StateFlow<List<TileId>> = tiles.combine(MutableStateFlow(TileId.entries)) { current, all ->
        val currentIds = current.map { it.id }.toSet()
        all.filter { it !in currentIds }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _isAddTileOpen = MutableStateFlow(false)
    val isAddTileOpen: StateFlow<Boolean> = _isAddTileOpen.asStateFlow()

    private val _selectedTileForSpecs = MutableStateFlow<TileId?>(null)
    val selectedTileForSpecs: StateFlow<TileId?> = _selectedTileForSpecs.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    fun toggleEditing() {
        _isEditing.value = !_isEditing.value
    }

    fun setEditing(editing: Boolean) {
        _isEditing.value = editing
    }

    fun setAddTileOpen(open: Boolean) {
        _isAddTileOpen.value = open
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun openSpecs(tileId: TileId) {
        _selectedTileForSpecs.value = tileId
    }

    fun closeSpecs() {
        _selectedTileForSpecs.value = null
    }

    fun resizeTile(tileId: TileId, newSize: TileSize) {
        viewModelScope.launch {
            val updated = tiles.value.map {
                if (it.id == tileId) it.copy(size = newSize) else it
            }
            preferencesRepo.saveDashboardTiles(updated)
        }
    }

    fun deleteTile(tileId: TileId) {
        viewModelScope.launch {
            val updated = tiles.value.filter { it.id != tileId }
            preferencesRepo.saveDashboardTiles(updated)
        }
    }

    fun addTile(tileId: TileId) {
        viewModelScope.launch {
            val defaultSize = when (tileId) {
                TileId.CPU, TileId.BATTERY -> TileSize.STANDARD
                TileId.MEMORY, TileId.THERMAL, TileId.NETWORK -> TileSize.WIDE
                TileId.STORAGE, TileId.DISPLAY -> TileSize.MINI
            }
            val newTile = TileConfig(tileId, defaultSize, tiles.value.size)
            val updated = tiles.value + newTile
            preferencesRepo.saveDashboardTiles(updated)
            _isAddTileOpen.value = false
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            preferencesRepo.resetDashboardToDefault()
        }
    }

    fun updateTemperatureUnit(unit: TemperatureUnit) {
        viewModelScope.launch { preferencesRepo.updateTemperatureUnit(unit) }
    }

    fun updateSpeedUnit(unit: SpeedUnit) {
        viewModelScope.launch { preferencesRepo.updateSpeedUnit(unit) }
    }

    fun updateAmoled(isAmoled: Boolean) {
        viewModelScope.launch { preferencesRepo.updateAmoledDark(isAmoled) }
    }
}