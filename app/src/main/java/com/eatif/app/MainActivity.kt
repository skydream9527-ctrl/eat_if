package com.eatif.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.eatif.app.ui.navigation.EatIfNavHost
import com.eatif.app.ui.theme.EatIfTheme
import com.eatif.app.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _darkModeFlow = MutableStateFlow(ThemeManager.isDarkMode)
    private val _followSystemFlow = MutableStateFlow(ThemeManager.followSystem)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkMode by _darkModeFlow.collectAsState()
            val followSystem by _followSystemFlow.collectAsState()

            EatIfTheme(
                darkTheme = if (followSystem) {
                    androidx.compose.foundation.isSystemInDarkTheme()
                } else {
                    isDarkMode
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EatIfNavHost(
                        onThemeChanged = { darkMode, followSystemTheme ->
                            ThemeManager.isDarkMode = darkMode
                            ThemeManager.followSystem = followSystemTheme
                            _darkModeFlow.value = darkMode
                            _followSystemFlow.value = followSystemTheme
                        }
                    )
                }
            }
        }
    }
}
