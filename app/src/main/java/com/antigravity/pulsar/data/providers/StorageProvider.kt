package com.antigravity.pulsar.data.providers

import android.os.Environment
import android.os.StatFs
import com.antigravity.pulsar.model.StorageState

class StorageProvider {

    fun getStorageState(): StorageState {
        return try {
            val path = Environment.getDataDirectory().path
            val stat = StatFs(path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)

            StorageState(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes
            )
        } catch (_: Throwable) {
            StorageState()
        }
    }
}