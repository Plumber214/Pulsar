package com.antigravity.pulsar.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import com.antigravity.pulsar.MainActivity
import com.antigravity.pulsar.data.TelemetryRepository
import com.antigravity.pulsar.data.preferences.UserPreferencesRepository
import com.antigravity.pulsar.model.TemperatureUnit
import kotlinx.coroutines.flow.first

class PulsarDualDialWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TelemetryRepository.getInstance(context)
        val cpu = repo.cpuState.value
        val thermal = repo.thermalState.value

        val prefsRepo = UserPreferencesRepository(context)
        val prefs = prefsRepo.userPreferencesFlow.first()
        val tempUnit = prefs.temperatureUnit

        // CPU Dial calculation
        val load = cpu.overallLoad
        val cpuColor = PulsarDialRenderer.getCpuColor(load)
        val cpuBitmap = PulsarDialRenderer.renderDialBitmap(
            progress = load / 100f,
            arcColor = cpuColor
        )
        val peakFreq = cpu.peakFreqMhz
        val freqText = if (peakFreq > 0) "%.1f GHz".format(peakFreq / 1000.0) else "Active"

        // Temp Dial calculation
        val socTempC = thermal.socTempCelsius
        val tempColor = PulsarDialRenderer.getTempColor(socTempC)
        val tempProgress = (socTempC / 95f).coerceIn(0f, 1f)
        val tempBitmap = PulsarDialRenderer.renderDialBitmap(
            progress = tempProgress,
            arcColor = tempColor
        )
        val displayTemp = if (tempUnit == TemperatureUnit.FAHRENHEIT) {
            "%.0f°F".format(socTempC * 9f / 5f + 32f)
        } else {
            "%.0f°C".format(socTempC)
        }
        val statusText = if (thermal.statusText.isNotBlank()) thermal.statusText.uppercase() else "NORMAL"

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF0F1218))
                        .cornerRadius(32.dp)
                        .padding(4.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: CPU
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            RoundDialContent(
                                bitmap = cpuBitmap,
                                category = "CPU",
                                mainValue = "${load.toInt()}%",
                                subText = freqText,
                                subTextColor = Color(cpuColor)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(4.dp))

                        // Right: Thermal
                        Box(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            RoundDialContent(
                                bitmap = tempBitmap,
                                category = "TEMP",
                                mainValue = displayTemp,
                                subText = statusText,
                                subTextColor = Color(tempColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

class PulsarDualDialWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulsarDualDialWidget()
}
