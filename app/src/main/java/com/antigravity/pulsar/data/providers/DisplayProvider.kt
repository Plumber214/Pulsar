package com.antigravity.pulsar.data.providers

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import com.antigravity.pulsar.model.DisplayState

class DisplayProvider(private val context: Context) {

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager

    fun getDisplayState(): DisplayState {
        val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
        val mode = display?.mode
        val refreshRate = display?.refreshRate ?: 60f

        val width = mode?.physicalWidth ?: 1080
        val height = mode?.physicalHeight ?: 2400

        val metrics = context.resources.displayMetrics
        val density = metrics.densityDpi

        val supportedModes = display?.supportedModes ?: emptyArray()
        val supportedRates = supportedModes.map { it.refreshRate }.distinct().sorted()

        val isHdr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            display?.isHdr == true
        } else false

        return DisplayState(
            refreshRateHz = refreshRate,
            widthPx = width,
            heightPx = height,
            densityDpi = density,
            isHdrSupported = isHdr,
            supportedRefreshRates = if (supportedRates.isNotEmpty()) supportedRates else listOf(60f, 120f)
        )
    }
}