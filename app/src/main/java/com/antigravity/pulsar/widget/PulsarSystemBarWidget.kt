package com.antigravity.pulsar.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.antigravity.pulsar.MainActivity
import com.antigravity.pulsar.data.TelemetryRepository

class PulsarSystemBarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repo = TelemetryRepository(context)
        val cpu = repo.cpuState.value
        val mem = repo.memoryState.value
        val battery = repo.batteryState.value
        val storage = repo.storageState.value

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .cornerRadius(20.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WidgetMetricItem("CPU", "${cpu.overallLoad.toInt()}%", GlanceModifier.defaultWeight())
                        WidgetMetricItem("RAM", "${mem.usedPercentage.toInt()}%", GlanceModifier.defaultWeight())
                        WidgetMetricItem("BAT", "${battery.levelPercentage}%", GlanceModifier.defaultWeight())
                        WidgetMetricItem("SSD", "${storage.usedPercentage.toInt()}%", GlanceModifier.defaultWeight())
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetMetricItem(label: String, value: String, modifier: GlanceModifier = GlanceModifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text = value,
            style = TextStyle(
                color = GlanceTheme.colors.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

class PulsarSystemBarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PulsarSystemBarWidget()
}