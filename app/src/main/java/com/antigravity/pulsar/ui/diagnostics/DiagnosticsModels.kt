package com.antigravity.pulsar.ui.diagnostics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class DiagnosticTestType(
    val title: String,
    val category: String,
    val description: String
) {
    DISPLAY_OLED("OLED Screen Sweeps", "Display & Input", "Test for dead pixels, burn-in, and color uniformity across pure RGBW colors."),
    MULTI_TOUCH("Multi-Touch Tracker", "Display & Input", "Real-time pointer tracker testing multi-finger contact count, coordinates, and pressure."),
    AUDIO_STEREO("Stereo Channel Audio", "Audio & Actuators", "Left and Right channel isolation sweep with synthesized 440Hz acoustic tones."),
    HAPTICS_MOTOR("Haptics Actuator", "Audio & Actuators", "Test vibration motor profiles: subtle click, double click, heavy tick, and pulse."),
    CAMERA_OPTICS("Camera2 Optics Inspector", "Camera", "Deep hardware inspection: physical sensor size, apertures, focal lengths, OIS, and RAW."),
    SENSORS_TOOL("Level & Precision Compass", "Sensors", "Real-time 3-axis spirit bubble level, cardinal compass heading, and barometric elevation.")
}

enum class DiagnosticStatus {
    UNTESTED,
    PASSED,
    FAILED
}

data class DiagnosticItemState(
    val type: DiagnosticTestType,
    val status: DiagnosticStatus = DiagnosticStatus.UNTESTED,
    val summary: String? = null
)
