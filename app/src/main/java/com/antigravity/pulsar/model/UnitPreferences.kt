package com.antigravity.pulsar.model

enum class TemperatureUnit(val symbol: String, val displayName: String) {
    FAHRENHEIT("°F", "Fahrenheit"),
    CELSIUS("°C", "Celsius");

    fun fromCelsius(celsius: Float): Float {
        return when (this) {
            CELSIUS -> celsius
            FAHRENHEIT -> (celsius * 9f / 5f) + 32f
        }
    }
}

enum class SpeedUnit(val displayName: String) {
    BYTES_PER_SEC("MB/s"),
    BITS_PER_SEC("Mbps")
}

enum class StorageUnit(val displayName: String) {
    DECIMAL("GB (1000)"),
    BINARY("GiB (1024)")
}

data class UserPreferences(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.FAHRENHEIT,
    val speedUnit: SpeedUnit = SpeedUnit.BYTES_PER_SEC,
    val storageUnit: StorageUnit = StorageUnit.DECIMAL,
    val isAmoledDark: Boolean = false,
    val telemetryIntervalMs: Long = 1500L
)