package com.aura.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aura.ai.core.ui.theme.AuraTheme
import com.aura.ai.navigation.AuraNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splash.setKeepOnScreenCondition { viewModel.uiState.value.isLoading }

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            AuraTheme(
                themeMode = state.preferences.themeMode,
                dynamicColor = state.preferences.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!state.isLoading) {
                        AuraNavHost(startSignedIn = state.isSignedIn)
                    }
                }
            }
        }
    }
}
