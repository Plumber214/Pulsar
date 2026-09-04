package com.antigravity.pulsar.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.theme.PulsarGreen

@Composable
fun DisplayTestScreen(
    onComplete: (DiagnosticStatus) -> Unit,
    onBack: () -> Unit
) {
    val colors = remember {
        listOf(
            "Pure Red" to Color(0xFFFF0000),
            "Pure Green" to Color(0xFF00FF00),
            "Pure Blue" to Color(0xFF0000FF),
            "Pure White" to Color(0xFFFFFFFF),
            "OLED Black" to Color(0xFF000000),
            "50% Neutral Gray" to Color(0xFF808080)
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var showControls by remember { mutableStateOf(true) }

    val currentColor = colors[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentColor.second)
            .clickable {
                if (currentIndex < colors.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }
            }
    ) {
        // Controls Overlay
        if (showControls) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xCC10141D),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = "OLED Sweep (${currentIndex + 1}/${colors.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = currentColor.first,
                                style = MaterialTheme.typography.labelSmall,
                                color = PulsarGreen
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { onComplete(DiagnosticStatus.FAILED) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fail", tint = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = { onComplete(DiagnosticStatus.PASSED) },
                            colors = ButtonDefaults.buttonColors(containerColor = PulsarGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Pass")
                            Text(text = "Pass", modifier = Modifier.padding(start = 4.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Bottom Hint
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xAA000000)
            ) {
                Text(
                    text = "Tap anywhere to advance color • Check for dead pixels",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
