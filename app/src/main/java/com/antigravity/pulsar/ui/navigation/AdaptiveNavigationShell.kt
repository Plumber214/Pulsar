package com.antigravity.pulsar.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.pulsar.theme.PulsarTeal
import com.antigravity.pulsar.ui.dashboard.DashboardScreen
import com.antigravity.pulsar.ui.dashboard.DashboardViewModel
import com.antigravity.pulsar.ui.diagnostics.DiagnosticsScreen

@Composable
fun AdaptiveNavigationShell(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthDp = maxWidth
        val isCompact = widthDp < 600.dp
        val columns = when {
            widthDp < 600.dp -> 2 // Compact Phone
            widthDp < 840.dp -> 3 // Medium Foldable
            else -> 4 // Tablet (Pixel Tablet)
        }

        if (isCompact) {
            // Phone Navigation: Bottom Navigation Bar
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard")
                            },
                            label = { Text("Dashboard") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PulsarTeal,
                                selectedTextColor = PulsarTeal,
                                indicatorColor = PulsarTeal.copy(alpha = 0.15f)
                            )
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(imageVector = Icons.Default.Build, contentDescription = "Diagnostics")
                            },
                            label = { Text("Diagnostics") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PulsarTeal,
                                selectedTextColor = PulsarTeal,
                                indicatorColor = PulsarTeal.copy(alpha = 0.15f)
                            )
                        )
                    }
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Crossfade(
                    targetState = selectedTab,
                    label = "PhoneTabCrossfade",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) { tab ->
                    if (tab == 0) {
                        DashboardScreen(
                            viewModel = viewModel,
                            maxColumns = columns
                        )
                    } else {
                        DiagnosticsScreen(viewModel = viewModel)
                    }
                }
            }
        } else {
            // Tablet Navigation: Left Navigation Rail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    NavigationRailItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard")
                        },
                        label = { Text("Dashboard") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = PulsarTeal,
                            selectedTextColor = PulsarTeal,
                            indicatorColor = PulsarTeal.copy(alpha = 0.15f)
                        )
                    )
                    NavigationRailItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(imageVector = Icons.Default.Build, contentDescription = "Diagnostics")
                        },
                        label = { Text("Diagnostics") },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = PulsarTeal,
                            selectedTextColor = PulsarTeal,
                            indicatorColor = PulsarTeal.copy(alpha = 0.15f)
                        )
                    )
                }

                Crossfade(
                    targetState = selectedTab,
                    label = "TabletTabCrossfade",
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) { tab ->
                    if (tab == 0) {
                        DashboardScreen(
                            viewModel = viewModel,
                            maxColumns = columns
                        )
                    } else {
                        DiagnosticsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}