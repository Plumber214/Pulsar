package com.antigravity.pulsar.data.providers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.antigravity.pulsar.model.SensorsState

class SensorProvider(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val pressureSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var latestPressureHpa: Float? = null
    private var latestLightLux: Float? = null
    private var latestX: Float = 0f
    private var latestY: Float = 0f
    private var latestZ: Float = 9.8f
    private var isListening = false

    val totalSensorsCount: Int
        get() = sensorManager?.getSensorList(Sensor.TYPE_ALL)?.size ?: 0

    fun startListening() {
        if (isListening || sensorManager == null) return
        isListening = true
        pressureSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        isListening = false
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_PRESSURE -> {
                latestPressureHpa = event.values.getOrNull(0)
            }
            Sensor.TYPE_LIGHT -> {
                latestLightLux = event.values.getOrNull(0)
            }
            Sensor.TYPE_ACCELEROMETER -> {
                latestX = event.values.getOrNull(0) ?: 0f
                latestY = event.values.getOrNull(1) ?: 0f
                latestZ = event.values.getOrNull(2) ?: 9.8f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getSensorsState(): SensorsState {
        if (!isListening) {
            startListening()
        }
        return SensorsState(
            pressureHpa = latestPressureHpa,
            lightLux = latestLightLux,
            accelX = latestX,
            accelY = latestY,
            accelZ = latestZ,
            totalSensorsCount = totalSensorsCount
        )
    }
}