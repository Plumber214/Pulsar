package com.antigravity.pulsar.data.providers

import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.antigravity.pulsar.model.ThermalState
import java.io.File

class ThermalProvider(private val context: Context, private val batteryProvider: BatteryProvider) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val history = ArrayDeque<Float>(30)

    fun getThermalState(): ThermalState {
        var headroom = 0.35f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val measured = powerManager?.getThermalHeadroom(30) ?: 0.35f
                if (measured >= 0f) {
                    headroom = measured.coerceIn(0f, 1.5f)
                }
            } catch (_: Throwable) {}
        }

        val statusText = when {
            headroom < 0.6f -> "Nominal"
            headroom < 0.85f -> "Moderate Load"
            headroom < 1.0f -> "Approaching Throttling"
            else -> "Severe Throttling Active"
        }

        val batteryTemp = batteryProvider.getBatteryState().temperatureCelsius
        val socTemp = readSocTempFallback(batteryTemp)
        val skinTemp = (batteryTemp * 0.92f).coerceAtLeast(20f)

        synchronized(history) {
            if (history.size >= 30) history.removeFirst()
            history.addLast(socTemp)
        }

        return ThermalState(
            throttlingHeadroom = headroom,
            statusText = statusText,
            socTempCelsius = socTemp,
            batteryTempCelsius = batteryTemp,
            skinTempCelsius = skinTemp,
            historyCelsius = history.toList()
        )
    }

    private fun readSocTempFallback(batteryTemp: Float): Float {
        return try {
            val zones = listOf(
                "/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp",
                "/sys/class/thermal/thermal_zone2/temp"
            )
            for (path in zones) {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val raw = file.readText().trim().toFloatOrNull() ?: 0f
                    val temp = if (raw > 1000f) raw / 1000f else raw
                    if (temp in 20f..115f) return temp
                }
            }
            batteryTemp + 4.5f
        } catch (_: Throwable) {
            batteryTemp + 4.5f
        }
    }
}