package com.antigravity.pulsar.ui.diagnostics

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.theme.PulsarBlue
import com.antigravity.pulsar.theme.PulsarGreen
import com.antigravity.pulsar.theme.PulsarMagenta
import com.antigravity.pulsar.theme.PulsarTeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioHapticsTestScreen(
    onComplete: (DiagnosticStatus) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun playSynthesizedTone(leftVol: Float, rightVol: Float, durationMs: Int = 1000) {
        coroutineScope.launch(Dispatchers.IO) {
            val sampleRate = 44100
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples * 2) // stereo

            val freq = 440.0 // 440 Hz Concert A
            for (i in 0 until numSamples) {
                val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                val sampleVal = (sin(angle) * Short.MAX_VALUE * 0.7).toInt().toShort()
                buffer[2 * i] = (sampleVal * leftVol).toInt().toShort()
                buffer[2 * i + 1] = (sampleVal * rightVol).toInt().toShort()
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf.coerceAtLeast(buffer.size * 2))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            track.play()
            track.write(buffer, 0, buffer.size)
            Thread.sleep(durationMs.toLong())
            track.stop()
            track.release()
        }
    }

    fun triggerVibration(effectId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator?.hasVibrator() == true) {
            vibrator.vibrate(VibrationEffect.createPredefined(effectId))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100L)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Audio & Haptics", fontWeight = FontWeight.Bold) },
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
            // Stereo Speaker Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Audio", tint = PulsarBlue)
                        Text(
                            text = "Stereo Sound Isolation Sweep",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Plays a clean synthesized 440 Hz sinusoidal tone to independently verify each physical speaker driver without channel crosstalk.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { playSynthesizedTone(1.0f, 0.0f) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Left Only")
                        }
                        FilledTonalButton(
                            onClick = { playSynthesizedTone(0.0f, 1.0f) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Right Only")
                        }
                        Button(
                            onClick = { playSynthesizedTone(1.0f, 1.0f) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PulsarBlue)
                        ) {
                            Text(text = "Stereo")
                        }
                    }
                }
            }

            // Haptic Actuator Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = "Haptics", tint = PulsarMagenta)
                        Text(
                            text = "Haptic Actuator Profiles",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Actuator: ${if (vibrator?.hasVibrator() == true) "Available" else "Not Detected"} • Amplitude Control: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator?.hasAmplitudeControl() == true) "Supported" else "Standard"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PulsarMagenta
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        triggerVibration(VibrationEffect.EFFECT_CLICK)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Subtle Click")
                            }
                            FilledTonalButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        triggerVibration(VibrationEffect.EFFECT_DOUBLE_CLICK)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Double Click")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        triggerVibration(VibrationEffect.EFFECT_HEAVY_CLICK)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Heavy Impact")
                            }
                            FilledTonalButton(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        triggerVibration(VibrationEffect.EFFECT_TICK)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Sharp Tick")
                            }
                        }
                    }
                }
            }
        }
    }
}
