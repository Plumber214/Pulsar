package com.antigravity.pulsar.ui.diagnostics

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.theme.PulsarGreen
import com.antigravity.pulsar.theme.PulsarTeal

data class InspectedCamera(
    val id: String,
    val facing: String,
    val megapixels: Float,
    val resolution: String,
    val sensorSizeMm: String,
    val focalLengthsMm: String,
    val apertures: String,
    val hasOis: Boolean,
    val hasRaw: Boolean,
    val hardwareLevel: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraSpecsScreen(
    onComplete: (DiagnosticStatus) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val cameras = remember {
        val list = mutableListOf<InspectedCamera>()
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            for (id in cameraManager.cameraIdList) {
                val chars = cameraManager.getCameraCharacteristics(id)
                val facingInt = chars.get(CameraCharacteristics.LENS_FACING)
                val facing = when (facingInt) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front (Selfie)"
                    CameraCharacteristics.LENS_FACING_BACK -> "Rear (Main/Tele/UW)"
                    else -> "External"
                }

                val pixelSize = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val mp = if (pixelSize != null) (pixelSize.width * pixelSize.height) / 1_000_000f else 0f
                val res = if (pixelSize != null) "${pixelSize.width}×${pixelSize.height}" else "Unknown"

                val physicalSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                val sensorMm = if (physicalSize != null) "%.2f × %.2f mm".format(physicalSize.width, physicalSize.height) else "N/A"

                val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                val focalStr = focalLengths?.joinToString(", ") { "%.1f mm".format(it) } ?: "Fixed"

                val apertures = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                val apertureStr = apertures?.joinToString(", ") { "f/%.2f".format(it) } ?: "N/A"

                val oisModes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                val hasOis = oisModes?.any { it == CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON } == true

                val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                val hasRaw = capabilities?.any { it == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW } == true

                val hwLevelInt = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                val hwLevel = when (hwLevelInt) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3 (Pro)"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full Camera2"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                    else -> "Legacy"
                }

                list.add(
                    InspectedCamera(
                        id = id,
                        facing = facing,
                        megapixels = mp,
                        resolution = res,
                        sensorSizeMm = sensorMm,
                        focalLengthsMm = focalStr,
                        apertures = apertureStr,
                        hasOis = hasOis,
                        hasRaw = hasRaw,
                        hardwareLevel = hwLevel
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Camera2 Optics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onComplete(DiagnosticStatus.FAILED) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fail", tint = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = { onComplete(DiagnosticStatus.PASSED) },
                        colors = ButtonDefaults.buttonColors(containerColor = PulsarGreen, contentColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Pass")
                        Text(text = "Pass", modifier = Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${cameras.size} Physical Camera Sensors Detected",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PulsarTeal
            )

            cameras.forEach { cam ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", tint = PulsarTeal)
                                Text(
                                    text = "Camera #${cam.id} • ${cam.facing}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PulsarTeal.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "%.1f MP".format(cam.megapixels),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PulsarTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Sensor Dimension", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = cam.sensorSizeMm, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Full Resolution", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = cam.resolution, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Focal Length", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = cam.focalLengthsMm, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Physical Aperture", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = cam.apertures, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Optical Stabilization (OIS)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (cam.hasOis) "Supported" else "None",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (cam.hasOis) PulsarGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "RAW Sensor DNG Output", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (cam.hasRaw) "Supported" else "No",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (cam.hasRaw) PulsarGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Camera2 Hardware Level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = cam.hardwareLevel, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                        }
                    }
                }
            }
        }
    }
}
