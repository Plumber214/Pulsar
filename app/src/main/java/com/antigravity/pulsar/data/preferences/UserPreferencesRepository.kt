package com.antigravity.pulsar.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.antigravity.pulsar.model.SpeedUnit
import com.antigravity.pulsar.model.StorageUnit
import com.antigravity.pulsar.model.TemperatureUnit
import com.antigravity.pulsar.model.TileConfig
import com.antigravity.pulsar.model.TileId
import com.antigravity.pulsar.model.TileSize
import com.antigravity.pulsar.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pulsar_settings")

class UserPreferencesRepository(private val context: Context) {

    private val KEY_TEMP_UNIT = stringPreferencesKey("temperature_unit")
    private val KEY_SPEED_UNIT = stringPreferencesKey("speed_unit")
    private val KEY_STORAGE_UNIT = stringPreferencesKey("storage_unit")
    private val KEY_AMOLED = booleanPreferencesKey("is_amoled_dark")
    private val KEY_INTERVAL = longPreferencesKey("telemetry_interval_ms")
    private val KEY_TILES = stringPreferencesKey("dashboard_tiles_config")

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            temperatureUnit = try {
                TemperatureUnit.valueOf(prefs[KEY_TEMP_UNIT] ?: TemperatureUnit.FAHRENHEIT.name)
            } catch (_: Throwable) { TemperatureUnit.FAHRENHEIT },
            speedUnit = try {
                SpeedUnit.valueOf(prefs[KEY_SPEED_UNIT] ?: SpeedUnit.BYTES_PER_SEC.name)
            } catch (_: Throwable) { SpeedUnit.BYTES_PER_SEC },
            storageUnit = try {
                StorageUnit.valueOf(prefs[KEY_STORAGE_UNIT] ?: StorageUnit.DECIMAL.name)
            } catch (_: Throwable) { StorageUnit.DECIMAL },
            isAmoledDark = prefs[KEY_AMOLED] ?: false,
            telemetryIntervalMs = prefs[KEY_INTERVAL] ?: 1500L
        )
    }

    val dashboardTilesFlow: Flow<List<TileConfig>> = context.dataStore.data.map { prefs ->
        val serialized = prefs[KEY_TILES]
        if (!serialized.isNullOrBlank()) {
            deserializeTiles(serialized)
        } else {
            defaultDashboardTiles()
        }
    }

    suspend fun updateTemperatureUnit(unit: TemperatureUnit) {
        context.dataStore.edit { it[KEY_TEMP_UNIT] = unit.name }
    }

    suspend fun updateSpeedUnit(unit: SpeedUnit) {
        context.dataStore.edit { it[KEY_SPEED_UNIT] = unit.name }
    }

    suspend fun updateStorageUnit(unit: StorageUnit) {
        context.dataStore.edit { it[KEY_STORAGE_UNIT] = unit.name }
    }

    suspend fun updateAmoledDark(isAmoled: Boolean) {
        context.dataStore.edit { it[KEY_AMOLED] = isAmoled }
    }

    suspend fun updateTelemetryInterval(intervalMs: Long) {
        context.dataStore.edit { it[KEY_INTERVAL] = intervalMs }
    }

    suspend fun saveDashboardTiles(tiles: List<TileConfig>) {
        val serialized = serializeTiles(tiles)
        context.dataStore.edit { it[KEY_TILES] = serialized }
    }

    suspend fun resetDashboardToDefault() {
        saveDashboardTiles(defaultDashboardTiles())
    }

    private fun serializeTiles(tiles: List<TileConfig>): String {
        return tiles.joinToString(";") { "${it.id.name},${it.size.name},${it.order}" }
    }

    private fun deserializeTiles(data: String): List<TileConfig> {
        return try {
            val deserialized = data.split(";").mapNotNull { entry ->
                val parts = entry.split(",")
                if (parts.size == 3) {
                    val id = TileId.valueOf(parts[0])
                    val size = TileSize.valueOf(parts[1])
                    val order = parts[2].toIntOrNull() ?: 0
                    TileConfig(id, size, order)
                } else null
            }
            if (deserialized.isEmpty()) {
                defaultDashboardTiles()
            } else {
                val existingIds = deserialized.map { it.id }.toSet()
                val defaults = defaultDashboardTiles()
                val missingDefaults = defaults.filter { it.id !in existingIds }
                if (missingDefaults.isNotEmpty()) {
                    var maxOrder = deserialized.maxOfOrNull { it.order } ?: 0
                    val appended = missingDefaults.map {
                        maxOrder++
                        it.copy(order = maxOrder)
                    }
                    deserialized + appended
                } else {
                    deserialized
                }
            }
        } catch (_: Throwable) {
            defaultDashboardTiles()
        }
    }

    fun defaultDashboardTiles(): List<TileConfig> {
        return listOf(
            TileConfig(TileId.CPU, TileSize.STANDARD, 0),
            TileConfig(TileId.BATTERY, TileSize.STANDARD, 1),
            TileConfig(TileId.MEMORY, TileSize.WIDE, 2),
            TileConfig(TileId.THERMAL, TileSize.WIDE, 3),
            TileConfig(TileId.NETWORK, TileSize.WIDE, 4),
            TileConfig(TileId.STORAGE, TileSize.MINI, 5),
            TileConfig(TileId.DISPLAY, TileSize.MINI, 6),
            TileConfig(TileId.GPU, TileSize.WIDE, 7),
            TileConfig(TileId.SENSORS, TileSize.WIDE, 8)
        )
    }
}