package com.antigravity.pulsar.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object PulsarWidgetUpdater {
    private const val TAG = "PulsarWidgetUpdater"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @Volatile
    private var isScreenReceiverRegistered = false
    private var lastUpdateTime = 0L
    private const val MIN_UPDATE_INTERVAL_MS = 2500L

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null) return
            val action = intent?.action
            if (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_USER_PRESENT) {
                Log.d(TAG, "Screen active ($action) - triggering widget update")
                updateAllWidgets(context.applicationContext, force = true)
            }
        }
    }

    /**
     * Registers a dynamic receiver for SCREEN_ON and USER_PRESENT so widgets refresh immediately
     * when the user turns on the display.
     */
    fun registerScreenReceiver(context: Context) {
        if (isScreenReceiverRegistered) return
        synchronized(this) {
            if (isScreenReceiverRegistered) return
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            try {
                context.applicationContext.registerReceiver(screenReceiver, filter)
                isScreenReceiverRegistered = true
                Log.d(TAG, "Registered screen broadcast receiver for widgets")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register screen receiver: ${e.message}")
            }
        }
    }

    /**
     * Updates all Pulsar home screen widgets.
     */
    fun updateAllWidgets(context: Context, force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastUpdateTime) < MIN_UPDATE_INTERVAL_MS) {
            return
        }
        lastUpdateTime = now

        scope.launch {
            try {
                PulsarCpuDialWidget().updateAll(context)
                PulsarTempDialWidget().updateAll(context)
                PulsarBatteryDialWidget().updateAll(context)
                PulsarRamDialWidget().updateAll(context)
                PulsarDualDialWidget().updateAll(context)
                PulsarSystemBarWidget().updateAll(context)
                PulsarMiniGaugeWidget().updateAll(context)
            } catch (e: Exception) {
                Log.w(TAG, "Error updating widgets: ${e.message}")
            }
        }
    }
}
