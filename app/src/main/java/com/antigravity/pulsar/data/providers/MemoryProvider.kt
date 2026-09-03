package com.antigravity.pulsar.data.providers

import android.app.ActivityManager
import android.content.Context
import com.antigravity.pulsar.model.MemoryState
import java.io.File

class MemoryProvider(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    fun getMemoryState(): MemoryState {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val meminfo = parseMemInfo()
        val totalRam = if (memoryInfo.totalMem > 0) memoryInfo.totalMem else meminfo["MemTotal"] ?: 8L * 1024 * 1024 * 1024
        val availRam = if (memoryInfo.availMem > 0) memoryInfo.availMem else meminfo["MemAvailable"] ?: (totalRam / 3)
        val usedRam = (totalRam - availRam).coerceAtLeast(0L)
        val cached = meminfo["Cached"] ?: 0L

        val swapTotal = meminfo["SwapTotal"] ?: 0L
        val swapFree = meminfo["SwapFree"] ?: 0L
        val swapUsed = (swapTotal - swapFree).coerceAtLeast(0L)

        val compressionRatio = calculateZramCompression()

        return MemoryState(
            totalRamBytes = totalRam,
            availableRamBytes = availRam,
            usedRamBytes = usedRam,
            cachedRamBytes = cached,
            totalSwapBytes = swapTotal,
            usedSwapBytes = swapUsed,
            zramCompressionRatio = compressionRatio,
            isLowMemory = memoryInfo.lowMemory,
            pageSizeKb = 4
        )
    }

    private fun parseMemInfo(): Map<String, Long> {
        val map = HashMap<String, Long>()
        try {
            val file = File("/proc/meminfo")
            if (file.exists() && file.canRead()) {
                file.forEachLine { line ->
                    val colonIndex = line.indexOf(':')
                    if (colonIndex > 0) {
                        val key = line.substring(0, colonIndex).trim()
                        val valueStr = line.substring(colonIndex + 1).replace("kB", "").trim()
                        val kb = valueStr.toLongOrNull() ?: 0L
                        map[key] = kb * 1024L
                    }
                }
            }
        } catch (_: Throwable) {}
        return map
    }

    private fun calculateZramCompression(): Float {
        return try {
            val orig = File("/sys/block/zram0/orig_data_size").readText().trim().toFloatOrNull() ?: 0f
            val compr = File("/sys/block/zram0/compr_data_size").readText().trim().toFloatOrNull() ?: 0f
            if (compr > 0f) orig / compr else 2.1f
        } catch (_: Throwable) {
            2.1f // Default representative compression factor
        }
    }
}