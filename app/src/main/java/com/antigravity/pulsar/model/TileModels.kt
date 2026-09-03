package com.antigravity.pulsar.model

enum class TileId(val title: String, val category: String) {
    CPU("Processor (CPU)", "Compute"),
    MEMORY("Memory & ZRAM", "Compute"),
    BATTERY("Battery & Power", "Energy"),
    THERMAL("Thermals & Headroom", "Sensors"),
    NETWORK("Network Speedometer", "Connectivity"),
    STORAGE("Internal Storage", "Storage"),
    DISPLAY("Display & Refresh Rate", "Display")
}

enum class TileSize(val colSpan: Int, val rowSpan: Int, val label: String) {
    MINI(1, 1, "1×1"),
    WIDE(2, 1, "2×1"),
    STANDARD(2, 2, "2×2"),
    DETAILED(4, 2, "Full");

    fun actualColSpan(maxColumns: Int): Int {
        return when (this) {
            MINI -> 1
            WIDE -> minOf(2, maxColumns)
            STANDARD -> minOf(2, maxColumns)
            DETAILED -> maxColumns
        }
    }
}

data class TileConfig(
    val id: TileId,
    val size: TileSize,
    val order: Int
)