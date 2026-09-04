package com.antigravity.pulsar

import android.app.Application
import com.antigravity.pulsar.widget.PulsarWidgetUpdater

class PulsarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PulsarWidgetUpdater.registerScreenReceiver(this)
        PulsarWidgetUpdater.updateAllWidgets(this)
    }
}