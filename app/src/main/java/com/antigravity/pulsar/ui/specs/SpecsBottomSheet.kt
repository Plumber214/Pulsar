package com.antigravity.pulsar.ui.specs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.BatteryState
import com.antigravity.pulsar.model.CpuState
import com.antigravity.pulsar.model.DeviceSpecs
import com.antigravity.pulsar.model.DisplayState
import com.antigravity.pulsar.model.MemoryState
import com.antigravity.pulsar.model.TileId
import com.antigravity.pulsar.theme.PulsarTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecsBottomSheet(
    specs: DeviceSpecs,
    cpuState: CpuState,
    memoryState: MemoryState,
    batteryState: BatteryState,
    displayState: DisplayState,
    gpuState: com.antigravity.pulsar.model.GpuState = com.antigravity.pulsar.model.GpuState(),
    sensorsState: com.antigravity.pulsar.model.SensorsState = com.antigravity.pulsar.model.SensorsState(),
    initialFocusTile: TileId?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hardware Diagnostics",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = specs.phoneModel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PulsarTeal
                    )
                }

                Button(
                    onClick = {
                        val report = generateMarkdownReport(specs, cpuState, memoryState, batteryState, displayState)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Pulsar Hardware Report", report))
                        Toast.makeText(context, "Hardware Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PulsarTeal, contentColor = MaterialTheme.colorScheme.surface)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                    Text(text = "Copy Report", modifier = Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    SpecCategoryCard("Processor & Silicon") {
                        SpecRow("SoC Model", specs.socMarketingName)
                        SpecRow("Manufacturer", specs.socManufacturer)
                        SpecRow("Platform / Board", "${specs.socPlatform} (${specs.boardCodename})")
                        SpecRow("Architecture", specs.cpuArchitecture)
                        SpecRow("Cores", "${cpuState.coreCount} Cores (Governor: ${cpuState.governor})")
                        SpecRow("Peak Frequency", "${cpuState.peakFreqMhz} MHz")
                    }
                }

                item {
                    SpecCategoryCard("Memory & Storage") {
                        val totalGb = memoryState.totalRamBytes / (1024f * 1024f * 1024f)
                        SpecRow("RAM Capacity", "%.1f GB".format(totalGb))
                        SpecRow("Memory Page Size", specs.memoryPageSize)
                        SpecRow("ZRAM Swap Total", "%.1f GB".format(memoryState.totalSwapBytes / (1024f * 1024f * 1024f)))
                        SpecRow("Compression Factor", "%.2fx".format(memoryState.zramCompressionRatio))
                    }
                }

                item {
                    SpecCategoryCard("Battery & Power") {
                        SpecRow("Design Capacity", "${batteryState.capacityMah} mAh")
                        SpecRow("Cycle Count", batteryState.cycleCount?.toString() ?: "Unavailable")
                        SpecRow("Health Status", batteryState.health)
                        SpecRow("Technology", batteryState.technology)
                        SpecRow("Nominal Voltage", "${batteryState.voltageMv} mV")
                    }
                }

                item {
                    SpecCategoryCard("Display & Surface") {
                        SpecRow("Resolution", "${displayState.widthPx} × ${displayState.heightPx} px")
                        SpecRow("Density", "${displayState.densityDpi} DPI")
                        SpecRow("Current Refresh Rate", "${displayState.refreshRateHz.toInt()} Hz")
                        SpecRow("Supported Modes", displayState.supportedRefreshRates.joinToString(", ") { "${it.toInt()}Hz" })
                        SpecRow("HDR Support", if (displayState.isHdrSupported) "Yes (HDR10+ / HLG)" else "Standard Dynamic Range")
                    }
                }

                item {
                    SpecCategoryCard("Graphics (GPU)") {
                        SpecRow("GPU Renderer", if (gpuState.renderer.isNotBlank()) gpuState.renderer else specs.gpuRenderer)
                        SpecRow("GPU Vendor", if (gpuState.vendor.isNotBlank()) gpuState.vendor else specs.gpuVendor)
                        SpecRow("Vulkan API", if (gpuState.vulkanVersion.isNotBlank()) gpuState.vulkanVersion else specs.vulkanVersion)
                        SpecRow("OpenGL ES Version", gpuState.glesVersion)
                        SpecRow("GLES Extensions", "${gpuState.extensionCount} Active")
                    }
                }

                item {
                    SpecCategoryCard("Sensor Array") {
                        SpecRow("Total Sensors", "${sensorsState.totalSensorsCount} Hardware Modules")
                        val p = sensorsState.pressureHpa
                        SpecRow("Barometer", if (p != null) "%.2f hPa".format(p) else "Not Equipped")
                        val lux = sensorsState.lightLux
                        SpecRow("Ambient Light", if (lux != null) "${lux.toInt()} Lux" else "Not Equipped")
                        SpecRow("Gravity Vector", "X: %.1f, Y: %.1f, Z: %.1f m/s²".format(sensorsState.accelX, sensorsState.accelY, sensorsState.accelZ))
                    }
                }

                item {
                    SpecCategoryCard("System & Firmware") {
                        SpecRow("Operating System", specs.androidVersion)
                        SpecRow("Security Patch", specs.securityPatch)
                        SpecRow("Kernel Version", specs.kernelVersion)
                        SpecRow("Manufacturer", specs.manufacturer)
                    }
                }
            }
        }
    }
}

@Composable
fun SpecCategoryCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = PulsarTeal,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun generateMarkdownReport(
    specs: DeviceSpecs,
    cpu: CpuState,
    mem: MemoryState,
    bat: BatteryState,
    disp: DisplayState
): String {
    return """
# Pulsar Hardware Diagnostics Report
- **Device**: ${specs.phoneModel} (${specs.boardCodename})
- **SoC**: ${specs.socMarketingName} (${specs.socPlatform})
- **CPU**: ${cpu.coreCount} Cores @ Peak ${cpu.peakFreqMhz} MHz (${specs.cpuArchitecture})
- **OS**: ${specs.androidVersion} | Security Patch: ${specs.securityPatch}
- **Kernel**: ${specs.kernelVersion}
- **RAM**: ${(mem.totalRamBytes / (1024f*1024f*1024f)).toInt()} GB (Page Size: ${specs.memoryPageSize})
- **Battery**: ${bat.levelPercentage}% | Cycles: ${bat.cycleCount ?: "N/A"} | Health: ${bat.health}
- **Display**: ${disp.widthPx}x${disp.heightPx} @ ${disp.refreshRateHz.toInt()}Hz (HDR: ${disp.isHdrSupported})
Generated by Pulsar Hardware Monitor.
""".trimIndent()
}