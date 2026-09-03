package com.antigravity.pulsar.model

data class CpuCoreState(
    val coreIndex: Int,
    val clusterName: String, // e.g., "Prime", "Performance", "Efficiency"
    val currentFreqMhz: Int,
    val maxFreqMhz: Int,
    val loadPercentage: Float,
    val isOnline: Boolean
)

data class CpuState(
    val overallLoad: Float = 0f,
    val currentAvgFreqMhz: Int = 0,
    val peakFreqMhz: Int = 0,
    val coreCount: Int = 8,
    val cores: List<CpuCoreState> = emptyList(),
    val loadHistory: List<Float> = emptyList(),
    val governor: String = "schedutil",
    val socMarketingName: String = "Detecting SoC...",
    val socPlatform: String = ""
)

data class MemoryState(
    val totalRamBytes: Long = 0L,
    val availableRamBytes: Long = 0L,
    val usedRamBytes: Long = 0L,
    val cachedRamBytes: Long = 0L,
    val totalSwapBytes: Long = 0L,
    val usedSwapBytes: Long = 0L,
    val zramCompressionRatio: Float = 1.0f,
    val isLowMemory: Boolean = false,
    val pageSizeKb: Int = 4
) {
    val usedPercentage: Float
        get() = if (totalRamBytes > 0) (usedRamBytes.toFloat() / totalRamBytes.toFloat()) * 100f else 0f
}

data class BatteryState(
    val levelPercentage: Int = 0,
    val isCharging: Boolean = false,
    val chargingCurrentMa: Int = 0,
    val chargingWatts: Float = 0f,
    val temperatureCelsius: Float = 0f,
    val voltageMv: Int = 0,
    val health: String = "Good",
    val technology: String = "Li-ion",
    val cycleCount: Int? = null,
    val capacityMah: Int = 0
)

data class ThermalState(
    val throttlingHeadroom: Float = 0f, // 0.0 (Cool) to 1.0 (Severe Throttling)
    val statusText: String = "Nominal",
    val socTempCelsius: Float = 0f,
    val batteryTempCelsius: Float = 0f,
    val skinTempCelsius: Float = 0f,
    val historyCelsius: List<Float> = emptyList()
)

data class NetworkState(
    val downloadBytesPerSec: Long = 0L,
    val uploadBytesPerSec: Long = 0L,
    val isWifiConnected: Boolean = false,
    val isCellularConnected: Boolean = false,
    val wifiSsid: String = "Disconnected",
    val wifiLinkSpeedMbps: Int = 0,
    val wifiFrequencyGhz: Float = 0f, // 2.4, 5.0, 6.0
    val cellularNetworkType: String = "Unknown",
    val downloadHistoryMb: List<Float> = emptyList(),
    val uploadHistoryMb: List<Float> = emptyList()
)

data class StorageState(
    val totalBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val usedBytes: Long = 0L
) {
    val usedPercentage: Float
        get() = if (totalBytes > 0) (usedBytes.toFloat() / totalBytes.toFloat()) * 100f else 0f
}

data class DisplayState(
    val refreshRateHz: Float = 60f,
    val widthPx: Int = 1080,
    val heightPx: Int = 2400,
    val densityDpi: Int = 420,
    val isHdrSupported: Boolean = true,
    val supportedRefreshRates: List<Float> = listOf(60f, 120f)
)

data class DeviceSpecs(
    val phoneModel: String = "",
    val manufacturer: String = "",
    val boardCodename: String = "",
    val androidVersion: String = "",
    val apiLevel: Int = 35,
    val securityPatch: String = "",
    val kernelVersion: String = "",
    val socMarketingName: String = "",
    val socManufacturer: String = "",
    val socPlatform: String = "",
    val cpuArchitecture: String = "",
    val armClustersDescription: String = "",
    val ramCapacityFormatted: String = "",
    val memoryPageSize: String = "4 KB"
)