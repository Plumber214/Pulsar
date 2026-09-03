package com.antigravity.pulsar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.antigravity.pulsar.theme.PulsarTheme
import com.antigravity.pulsar.ui.dashboard.DashboardViewModel
import com.antigravity.pulsar.ui.navigation.AdaptiveNavigationShell

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs by viewModel.preferences.collectAsState()

            PulsarTheme(isAmoled = prefs.isAmoledDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AdaptiveNavigationShell(viewModel = viewModel)
                }
            }
        }
    }
}