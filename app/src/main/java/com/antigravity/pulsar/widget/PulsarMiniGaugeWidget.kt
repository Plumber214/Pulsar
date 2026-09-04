package com.antigravity.pulsar.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import com.antigravity.pulsar.data.TelemetryRepository

class PulsarMiniGaugeWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TelemetryRepository.getInstance(context)
        val cpu = repo.cpuState.value

        val load = cpu.overallLoad
        val arcColor = PulsarDialRenderer.getCpuColor(load)
        val bitmap = PulsarDialRenderer.renderDialBitmap(
            progress = load / 100f,
            arcColor = arcColor
        )

        val peakFreq = cpu.peakFreqMhz
        val freqText = if (peakFreq > 0) "%.1f GHz".format(peakFreq / 1000.0) else "Active"

        provideContent {
            RoundDialContent(
                bitmap = bitmap,
                category = "CPU",
                mainValue = "${load.toInt()}%",
                subText = freqText,
                subTextColor = Color(arcColor)
            )
        }
    }
}

class PulsarMiniGaugeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulsarMiniGaugeWidget()
}