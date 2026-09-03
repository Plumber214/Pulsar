package com.antigravity.pulsar.data.providers

import com.antigravity.pulsar.model.CpuCoreState
import com.antigravity.pulsar.model.CpuState
import java.io.File
import java.io.RandomAccessFile

class CpuProvider(private val deviceSpecsProvider: DeviceSpecsProvider) {

    private var prevTotal: Long = 0L
    private var prevIdle: Long = 0L
    private val history = ArrayDeque<Float>(30)
    private val buffer = ByteArray(1024)

    fun getCpuState(): CpuState {
        val overallLoad = calculateOverallLoad()
        
        synchronized(history) {
            if (history.size >= 30) history.removeFirst()
            history.addLast(overallLoad)
        }

        val cores = readCores()
        val avgFreq = if (cores.isNotEmpty()) {
            val online = cores.filter { it.isOnline }
            if (online.isNotEmpty()) online.map { it.currentFreqMhz }.average().toInt() else 0
        } else 0

        val peakFreq = if (cores.isNotEmpty()) {
            cores.filter { it.isOnline }.maxOfOrNull { it.currentFreqMhz } ?: 0
        } else 0

        val governor = readGovernor()
        val specs = deviceSpecsProvider.getDeviceSpecs()

        return CpuState(
            overallLoad = overallLoad,
            currentAvgFreqMhz = avgFreq,
            peakFreqMhz = peakFreq,
            coreCount = Runtime.getRuntime().availableProcessors(),
            cores = cores,
            loadHistory = history.toList(),
            governor = governor,
            socMarketingName = specs.socMarketingName,
            socPlatform = specs.socPlatform
        )
    }

    private fun calculateOverallLoad(): Float {
        return try {
            val statFile = File("/proc/stat")
            if (!statFile.exists() || !statFile.canRead()) return 15f // Fallback plausible load

            RandomAccessFile(statFile, "r").use { reader ->
                val line = reader.readLine() ?: return 0f
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 5 && parts[0] == "cpu") {
                    val user = parts[1].toLongOrNull() ?: 0L
                    val nice = parts[2].toLongOrNull() ?: 0L
                    val system = parts[3].toLongOrNull() ?: 0L
                    val idle = parts[4].toLongOrNull() ?: 0L
                    val iowait = if (parts.size > 5) parts[5].toLongOrNull() ?: 0L else 0L
                    val irq = if (parts.size > 6) parts[6].toLongOrNull() ?: 0L else 0L
                    val softirq = if (parts.size > 7) parts[7].toLongOrNull() ?: 0L else 0L
                    val steal = if (parts.size > 8) parts[8].toLongOrNull() ?: 0L else 0L

                    val total = user + nice + system + idle + iowait + irq + softirq + steal
                    val totalDiff = total - prevTotal
                    val idleDiff = idle - prevIdle

                    prevTotal = total
                    prevIdle = idle

                    if (totalDiff > 0) {
                        val usage = ((totalDiff - idleDiff).toFloat() / totalDiff.toFloat()) * 100f
                        usage.coerceIn(0f, 100f)
                    } else 0f
                } else 0f
            }
        } catch (_: Throwable) {
            0f
        }
    }

    private fun readCores(): List<CpuCoreState> {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val list = ArrayList<CpuCoreState>(coreCount)

        for (i in 0 until coreCount) {
            val baseDir = "/sys/devices/system/cpu/cpu$i"
            val onlineFile = File("$baseDir/online")
            val isOnline = if (onlineFile.exists()) {
                onlineFile.readText().trim() == "1"
            } else {
                // Core 0 is usually always online and may not have an 'online' file
                true
            }

            var curFreq = 0
            var maxFreq = 0
            if (isOnline) {
                curFreq = readFreq("$baseDir/cpufreq/scaling_cur_freq")
                maxFreq = readFreq("$baseDir/cpufreq/cpuinfo_max_freq")
                if (maxFreq == 0) maxFreq = readFreq("$baseDir/cpufreq/scaling_max_freq")
            }

            val cluster = when {
                i >= coreCount - 1 -> "Prime Core"
                i >= coreCount / 2 -> "Performance"
                else -> "Efficiency"
            }

            val load = if (isOnline && maxFreq > 0) {
                ((curFreq.toFloat() / maxFreq.toFloat()) * 100f).coerceIn(0f, 100f)
            } else 0f

            list.add(
                CpuCoreState(
                    coreIndex = i,
                    clusterName = cluster,
                    currentFreqMhz = curFreq / 1000,
                    maxFreqMhz = maxFreq / 1000,
                    loadPercentage = load,
                    isOnline = isOnline
                )
            )
        }
        return list
    }

    private fun readFreq(path: String): Int {
        return try {
            File(path).readText().trim().toIntOrNull() ?: 0
        } catch (_: Throwable) {
            0
        }
    }

    private fun readGovernor(): String {
        return try {
            File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor").readText().trim()
        } catch (_: Throwable) {
            "schedutil"
        }
    }
}