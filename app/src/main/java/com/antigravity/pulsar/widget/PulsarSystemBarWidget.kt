package com.antigravity.pulsar.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.antigravity.pulsar.MainActivity
import com.antigravity.pulsar.data.TelemetryRepository

class PulsarSystemBarWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TelemetryRepository.getInstance(context)
        val cpu = repo.cpuState.value
        val mem = repo.memoryState.value
        val battery = repo.batteryState.value
        val storage = repo.storageState.value

        val cpuColor = PulsarDialRenderer.getCpuColor(cpu.overallLoad)
        val ramColor = PulsarDialRenderer.getRamColor(mem.usedPercentage)
        val batColor = PulsarDialRenderer.getBatteryColor(battery.levelPercentage, battery.isCharging)
        val ssdColor = Color(0xFF29B6F6) // Light Azure

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF0F1218))
                        .cornerRadius(24.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SystemBarMetricPill(
                            label = "CPU",
                            value = "${cpu.overallLoad.toInt()}%",
                            accentColor = Color(cpuColor),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        SystemBarMetricPill(
                            label = "RAM",
                            value = "${mem.usedPercentage.toInt()}%",
                            accentColor = Color(ramColor),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        SystemBarMetricPill(
                            label = "BAT",
                            value = "${battery.levelPercentage}%",
                            accentColor = Color(batColor),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        SystemBarMetricPill(
                            label = "SSD",
                            value = "${storage.usedPercentage.toInt()}%",
                            accentColor = ssdColor,
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemBarMetricPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF161B22))
            .cornerRadius(16.dp)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(Color(0xFF8B949E)),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(1.dp))
            Text(
                text = value,
                style = TextStyle(
                    color = ColorProvider(accentColor),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

class PulsarSystemBarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulsarSystemBarWidget()
}