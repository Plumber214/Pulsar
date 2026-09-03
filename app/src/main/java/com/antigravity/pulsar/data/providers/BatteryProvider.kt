package com.antigravity.pulsar.data.providers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.antigravity.pulsar.model.BatteryState
import java.io.File
import kotlin.math.abs

class BatteryProvider(private val context: Context) {

    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

    fun getBatteryState(): BatteryState {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, intentFilter)

        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val levelPct = if (level >= 0 && scale > 0) (level * 100) / scale else 50

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250) ?: 250
        val tempCelsius = tempTenths / 10f

        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000

        var currentMicroAmps = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 0
        if (abs(currentMicroAmps) > 10_000_000) {
            currentMicroAmps /= 1000
        }
        val currentMa = currentMicroAmps / 1000

        val watts = (voltageMv / 1000f) * (abs(currentMa) / 1000f)

        val healthInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD) ?: BatteryManager.BATTERY_HEALTH_GOOD
        val health = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Good"
        }

        val technology = batteryIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        val cycleCount = if (Build.VERSION.SDK_INT >= 34) {
            val cycles = batteryIntent?.getIntExtra("android.os.extra.CYCLE_COUNT", -1) ?: -1
            if (cycles >= 0) cycles else readCycleCountFallback()
        } else {
            readCycleCountFallback()
        }

        val capacityMah = detectBatteryCapacity()

        return BatteryState(
            levelPercentage = levelPct,
            isCharging = isCharging,
            chargingCurrentMa = currentMa,
            chargingWatts = watts,
            temperatureCelsius = tempCelsius,
            voltageMv = voltageMv,
            health = health,
            technology = technology,
            cycleCount = cycleCount,
            capacityMah = capacityMah
        )
    }

    private fun readCycleCountFallback(): Int? {
        return try {
            val file = File("/sys/class/power_supply/battery/cycle_count")
            if (file.exists() && file.canRead()) {
                file.readText().trim().toIntOrNull()
            } else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun detectBatteryCapacity(): Int {
        return try {
            val profile = Class.forName("com.android.internal.os.PowerProfile")
                .getConstructor(Context::class.java)
                .newInstance(context)
            val cap = Class.forName("com.android.internal.os.PowerProfile")
                .getMethod("getBatteryCapacity")
                .invoke(profile) as? Double
            cap?.toInt() ?: 5000
        } catch (_: Throwable) {
            5000
        }
    }
}