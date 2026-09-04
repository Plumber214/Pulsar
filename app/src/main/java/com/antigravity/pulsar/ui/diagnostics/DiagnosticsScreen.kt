package com.antigravity.pulsar.ui.diagnostics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.theme.PulsarBlue
import com.antigravity.pulsar.theme.PulsarGreen
import com.antigravity.pulsar.theme.PulsarMagenta
import com.antigravity.pulsar.theme.PulsarOrange
import com.antigravity.pulsar.theme.PulsarTeal
import com.antigravity.pulsar.ui.dashboard.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val sensorsState by viewModel.sensorsState.collectAsState()

    var activeTest by remember { mutableStateOf<DiagnosticTestType?>(null) }
    val testResults = remember {
        mutableStateMapOf<DiagnosticTestType, DiagnosticStatus>().apply {
            DiagnosticTestType.entries.forEach { this[it] = DiagnosticStatus.UNTESTED }
        }
    }

    if (activeTest != null) {
        when (activeTest) {
            DiagnosticTestType.DISPLAY_OLED -> DisplayTestScreen(
                onComplete = {
                    testResults[DiagnosticTestType.DISPLAY_OLED] = it
                    activeTest = null
                },
                onBack = { activeTest = null }
            )
            DiagnosticTestType.MULTI_TOUCH -> MultiTouchTestScreen(
                onComplete = {
                    testResults[DiagnosticTestType.MULTI_TOUCH] = it
                    activeTest = null
                },
                onBack = { activeTest = null }
            )
            DiagnosticTestType.AUDIO_STEREO, DiagnosticTestType.HAPTICS_MOTOR -> AudioHapticsTestScreen(
                onComplete = {
                    testResults[activeTest!!] = it
                    activeTest = null
                },
                onBack = { activeTest = null }
            )
            DiagnosticTestType.CAMERA_OPTICS -> CameraSpecsScreen(
                onComplete = {
                    testResults[DiagnosticTestType.CAMERA_OPTICS] = it
                    activeTest = null
                },
                onBack = { activeTest = null }
            )
            DiagnosticTestType.SENSORS_TOOL -> SensorsToolScreen(
                state = sensorsState,
                onComplete = {
                    testResults[DiagnosticTestType.SENSORS_TOOL] = it
                    activeTest = null
                },
                onBack = { activeTest = null }
            )
            null -> {}
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Diagnostics Suite",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val passCount = testResults.values.count { it == DiagnosticStatus.PASSED }
                        Text(
                            text = "$passCount of ${DiagnosticTestType.entries.size} Verified",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (passCount > 0) PulsarGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(DiagnosticTestType.entries) { testType ->
                val status = testResults[testType] ?: DiagnosticStatus.UNTESTED
                val (icon, color) = when (testType) {
                    DiagnosticTestType.DISPLAY_OLED -> Icons.Default.Tv to PulsarTeal
                    DiagnosticTestType.MULTI_TOUCH -> Icons.Default.TouchApp to PulsarGreen
                    DiagnosticTestType.AUDIO_STEREO -> Icons.Default.VolumeUp to PulsarBlue
                    DiagnosticTestType.HAPTICS_MOTOR -> Icons.Default.VolumeUp to PulsarMagenta
                    DiagnosticTestType.CAMERA_OPTICS -> Icons.Default.CameraAlt to PulsarOrange
                    DiagnosticTestType.SENSORS_TOOL -> Icons.Default.Sensors to PulsarTeal
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeTest = testType },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = color.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = icon, contentDescription = testType.title, tint = color, modifier = Modifier.size(24.dp))
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = testType.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = testType.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (status) {
                                DiagnosticStatus.PASSED -> PulsarGreen.copy(alpha = 0.18f)
                                DiagnosticStatus.FAILED -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                                DiagnosticStatus.UNTESTED -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                val statusIcon = when (status) {
                                    DiagnosticStatus.PASSED -> Icons.Default.CheckCircle
                                    DiagnosticStatus.FAILED -> Icons.Default.Error
                                    DiagnosticStatus.UNTESTED -> Icons.Default.HelpOutline
                                }
                                val statusTint = when (status) {
                                    DiagnosticStatus.PASSED -> PulsarGreen
                                    DiagnosticStatus.FAILED -> MaterialTheme.colorScheme.error
                                    DiagnosticStatus.UNTESTED -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Icon(imageVector = statusIcon, contentDescription = null, tint = statusTint, modifier = Modifier.size(14.dp))
                                Text(
                                    text = status.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = statusTint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
