package com.antigravity.pulsar.data.providers

import android.content.Context
import android.os.Build
import android.os.SystemClock
import com.antigravity.pulsar.model.DeviceSpecs
import java.io.File
import java.io.RandomAccessFile

class DeviceSpecsProvider(private val context: Context) {

    fun getDeviceSpecs(): DeviceSpecs {
        val socInfo = resolveSocIdentity()
        val kernelVersion = readKernelVersion()
        val pageSize = detectPageSize()
        val clusters = detectCpuClusters()

        return DeviceSpecs(
            phoneModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            manufacturer = Build.MANUFACTURER,
            boardCodename = Build.BOARD,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            apiLevel = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH ?: "Unknown",
            kernelVersion = kernelVersion,
            socMarketingName = socInfo.first,
            socManufacturer = socInfo.second,
            socPlatform = socInfo.third,
            cpuArchitecture = System.getProperty("os.arch") ?: "arm64-v8a",
            armClustersDescription = clusters,
            ramCapacityFormatted = "",
            memoryPageSize = pageSize
        )
    }

    private fun resolveSocIdentity(): Triple<String, String, String> {
        var socModel = ""
        var socManufacturer = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                socModel = Build.SOC_MODEL
                socManufacturer = Build.SOC_MANUFACTURER
            } catch (_: Throwable) {}
        }

        val propModel = getSystemProperty("ro.soc.model")
        val propManufacturer = getSystemProperty("ro.soc.manufacturer")
        val propBoard = getSystemProperty("ro.board.platform")
        val propHardware = Build.HARDWARE

        val platform = propModel.ifEmpty { propBoard.ifEmpty { propHardware } }

        val detectedMarketingName = mapPlatformToCommercialName(platform, Build.BOARD, Build.MODEL)
        
        val finalName = if (socModel.isNotBlank() && !socModel.equals("unknown", ignoreCase = true)) {
            socModel
        } else if (detectedMarketingName.isNotBlank()) {
            detectedMarketingName
        } else if (platform.isNotBlank()) {
            "Platform: $platform"
        } else {
            "Generic ARM SoC"
        }

        val finalManufacturer = if (socManufacturer.isNotBlank() && !socManufacturer.equals("unknown", ignoreCase = true)) {
            socManufacturer
        } else {
            detectManufacturerFromPlatform(platform, finalName)
        }

        return Triple(finalName, finalManufacturer, platform)
    }

    private fun mapPlatformToCommercialName(platform: String, board: String, model: String): String {
        val lower = "${platform.lowercase()} ${board.lowercase()} ${model.lowercase()}"
        return when {
            // Google Tensor Generations
            lower.contains("malibu") -> "Google Tensor G6"
            lower.contains("laguna") -> "Google Tensor G5"
            lower.contains("zumapro") -> "Google Tensor G4"
            lower.contains("zuma") -> "Google Tensor G3"
            lower.contains("cheetah") || lower.contains("panther") || lower.contains("cloudripper") -> "Google Tensor G2"
            lower.contains("whitechapel") || lower.contains("raven") || lower.contains("oriole") -> "Google Tensor (G1)"
            
            // Qualcomm Snapdragon 8 Gen series
            lower.contains("sm8750") || lower.contains("sun") -> "Snapdragon 8 Elite"
            lower.contains("sm8650") || lower.contains("pineapple") -> "Snapdragon 8 Gen 3"
            lower.contains("sm8550") || lower.contains("kalama") -> "Snapdragon 8 Gen 2"
            lower.contains("sm8475") -> "Snapdragon 8+ Gen 1"
            lower.contains("sm8450") || lower.contains("taro") -> "Snapdragon 8 Gen 1"
            
            // MediaTek Dimensity 9000 series
            lower.contains("mt6991") -> "MediaTek Dimensity 9400"
            lower.contains("mt6989") -> "MediaTek Dimensity 9300"
            lower.contains("mt6985") -> "MediaTek Dimensity 9200"

            else -> ""
        }
    }

    private fun detectManufacturerFromPlatform(platform: String, name: String): String {
        return when {
            name.contains("Tensor", ignoreCase = true) -> "Google"
            name.contains("Snapdragon", ignoreCase = true) || platform.startsWith("sm", ignoreCase = true) || platform.startsWith("qcom", ignoreCase = true) -> "Qualcomm"
            name.contains("Dimensity", ignoreCase = true) || platform.startsWith("mt", ignoreCase = true) -> "MediaTek"
            name.contains("Exynos", ignoreCase = true) || platform.startsWith("exynos", ignoreCase = true) -> "Samsung"
            else -> Build.MANUFACTURER
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val clazz = Class.forName("android.os.SystemProperties")
            val getMethod = clazz.getMethod("get", String::class.java)
            (getMethod.invoke(null, key) as? String) ?: ""
        } catch (_: Throwable) {
            ""
        }
    }

    private fun readKernelVersion(): String {
        return try {
            File("/proc/version").readText().lines().firstOrNull()?.split(" ")?.take(3)?.joinToString(" ") ?: "Linux Kernel"
        } catch (_: Throwable) {
            System.getProperty("os.version") ?: "Linux"
        }
    }

    private fun detectPageSize(): String {
        return try {
            val pageSize = try {
                val osClass = Class.forName("android.system.Os")
                val sysconfMethod = osClass.getMethod("sysconf", Int::class.javaPrimitiveType)
                val scPageSizeField = Class.forName("android.system.OsConstants").getField("_SC_PAGESIZE")
                val scPageSize = scPageSizeField.getInt(null)
                (sysconfMethod.invoke(null, scPageSize) as? Long) ?: 4096L
            } catch (_: Throwable) {
                4096L
            }
            if (pageSize >= 16384L) "16 KB (Modern Page Size)" else "4 KB (Standard Page Size)"
        } catch (_: Throwable) {
            "4 KB"
        }
    }

    private fun detectCpuClusters(): String {
        val totalCores = Runtime.getRuntime().availableProcessors()
        return "$totalCores Cores Heterogeneous Architecture"
    }
}