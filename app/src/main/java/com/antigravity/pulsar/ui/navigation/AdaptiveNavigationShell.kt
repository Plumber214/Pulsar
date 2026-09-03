package com.antigravity.pulsar.ui.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.pulsar.ui.dashboard.DashboardScreen
import com.antigravity.pulsar.ui.dashboard.DashboardViewModel

@Composable
fun AdaptiveNavigationShell(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthDp = maxWidth
        val columns = when {
            widthDp < 600.dp -> 2 // Compact (Standard Phones)
            widthDp < 840.dp -> 3 // Medium (Foldables unfolded, small tablets)
            else -> 4 // Expanded (Large tablets, Pixel Tablet, DeX)
        }

        DashboardScreen(
            viewModel = viewModel,
            maxColumns = columns
        )
    }
}