package com.antigravity.pulsar.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
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
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.antigravity.pulsar.MainActivity
import com.antigravity.pulsar.data.TelemetryRepository
import com.antigravity.pulsar.data.preferences.UserPreferencesRepository
import com.antigravity.pulsar.model.TemperatureUnit
import kotlinx.coroutines.flow.first

// --- Common Round Dial Composable ---

@Composable
fun RoundDialContent(
    bitmap: Bitmap,
    category: String,
    mainValue: String,
    subText: String,
    subTextColor: Color
) {
    val size = LocalSize.current
    val isLarge = size.width >= 105.dp

    val categorySize = if (isLarge) 10.sp else 8.5.sp
    val mainSize = if (isLarge) 26.sp else 19.sp
    val subSize = if (isLarge) 9.5.sp else 8.sp

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF0F1218))
                .cornerRadius(999.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            // Dial arc background image
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "$category Dial Gauge",
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(2.dp)
            )

            // Centered telemetry readout, adjusted for optical center of 270° arc
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = GlanceModifier.height(if (isLarge) 4.dp else 2.dp))
                Text(
                    text = category,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF94A3B8)),
                        fontSize = categorySize,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = mainValue,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = mainSize,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = subText,
                    style = TextStyle(
                        color = ColorProvider(subTextColor),
                        fontSize = subSize,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = GlanceModifier.height(if (isLarge) 12.dp else 7.dp))
            }
        }
    }
}

// --- 1. CPU Dial Widget ---

class PulsarCpuDialWidget : GlanceAppWidget() {
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

class PulsarCpuDialWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulsarCpuDialWidget()
}

// --- 2. Thermal Dial Widget ---

class PulsarTempDialWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TelemetryRepository.getInstance(context)
        val thermal = repo.thermalState.value

        val prefsRepo = UserPreferencesRepository(context)
        val prefs = prefsRepo.userPreferencesFlow.first()
        val tempUnit = prefs.temperatureUnit

        val socTempC = thermal.socTempCelsius
        val arcColor = PulsarDialRenderer.getTempColor(socTempC)
        val progress = (socTempC / 95f).coerceIn(0f, 1f)
        val bitmap = PulsarDialRenderer.renderDialBitmap(
            progress = progress,
            arcColor = arcColor
        )

        val displayTemp = if (tempUnit == TemperatureUnit.FAHRENHEIT) {
            "%.0f°F".format(socTempC * 9f / 5f + 32f)
        } else {
            "%.0f°C".format(socTempC)
        }

        val status = if (thermal.statusText.isNotBlank()) thermal.statusText.uppercase() else "NORMAL"

        provideContent {
            RoundDialContent(
                bitmap = bitmap,
                category = "TEMP",
                mainValue = displayTemp,
                subText = status,
                subTextColor = Color(arcColor)
            )
        }
    }
}

class PulsarTempDialWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulsarTempDialWidget()
}

// --- 3. Battery Dial Widget ---

class PulsarBatteryDialWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TelemetryRepository.getInstance(context)
        val battery = repo.batteryState.value

        val level = battery.levelPercentage
        val isCharging = battery.isCharging
        val arcColor = PulsarDialRenderer.getBatteryColor(level, isCharging)
        val bitmap = PulsarDialRenderer.renderDialBitmap(
            progress = level / 100f,
            arcColor = arcColor
        )

        val subText = when {
            isCharging && battery.chargingWatts > 0f -> "+%.1fW".format(battery.chargingWatts)
            isCharging -> "Charging"
            else -> "Discharge"
        }

        provideContent {
            RoundDialContent(
                bitmap = bitmap,
                category = "BAT",
                mainValue = "$level%",
                subText = subText,
                subTextColor = Color(arcColor)
            )
        }
    }
}

class PulsarBatteryDialWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulsarBatteryDialWidget()
}

// --- 4. RAM Dial Widget ---

class PulsarRamDialWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TelemetryRepository.getInstance(context)
        val mem = repo.memoryState.value

        val usedPct = mem.usedPercentage
        val arcColor = PulsarDialRenderer.getRamColor(usedPct)
        val bitmap = PulsarDialRenderer.renderDialBitmap(
            progress = usedPct / 100f,
            arcColor = arcColor
        )

        val usedGb = mem.usedRamBytes / (1024.0 * 1024.0 * 1024.0)
        val totalGb = mem.totalRamBytes / (1024.0 * 1024.0 * 1024.0)
        val subText = "%.1f/%.0fGB".format(usedGb, totalGb)

        provideContent {
            RoundDialContent(
                bitmap = bitmap,
                category = "RAM",
                mainValue = "${usedPct.toInt()}%",
                subText = subText,
                subTextColor = Color(arcColor)
            )
        }
    }
}

class PulsarRamDialWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulsarRamDialWidget()
}
