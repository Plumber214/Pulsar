package com.antigravity.pulsar.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.pulsar.model.SpeedUnit
import com.antigravity.pulsar.model.TemperatureUnit
import com.antigravity.pulsar.model.UserPreferences
import com.antigravity.pulsar.theme.PulsarTeal

@Composable
fun SettingsDialog(
    preferences: UserPreferences,
    onTempUnitChange: (TemperatureUnit) -> Unit,
    onSpeedUnitChange: (SpeedUnit) -> Unit,
    onAmoledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pulsar Preferences",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Temperature Unit
                Column {
                    Text(
                        text = "Temperature Unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TemperatureUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = preferences.temperatureUnit == unit,
                                onClick = { onTempUnitChange(unit) },
                                label = { Text("${unit.symbol} (${unit.displayName})") }
                            )
                        }
                    }
                }

                // Speed Unit
                Column {
                    Text(
                        text = "Network Speed Unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpeedUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = preferences.speedUnit == unit,
                                onClick = { onSpeedUnitChange(unit) },
                                label = { Text(unit.displayName) }
                            )
                        }
                    }
                }

                // AMOLED True Black Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AMOLED True Black",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Pure #000000 background for power saving on OLED screens",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = preferences.isAmoledDark,
                        onCheckedChange = onAmoledChange
                    )
                }

                // Privacy Note
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🛡️ 100% Offline & Private",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PulsarTeal
                        )
                        Text(
                            text = "Pulsar performs all diagnostics on-device. Zero metrics leave your hardware.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold, color = PulsarTeal)
            }
        }
    )
}