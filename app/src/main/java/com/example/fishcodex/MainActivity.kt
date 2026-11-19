package com.example.fishcodex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val vm: CodexViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val font = loadAppFont(this)
        val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)

        setContent {
            val settings by vm.settings.collectAsStateWithLifecycle()
            val dark = isDarkTheme(settings.themeMode)

            // 沉浸式系统栏；状态栏/导航栏图标颜色跟随"应用内"的深浅色设置，而不是只看系统
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { dark },
                    navigationBarStyle = SystemBarStyle.auto(lightScrim, darkScrim) { dark },
                )
                onDispose { }
            }

            FishTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
                fontFamily = font,
            ) {
                CodexApp(vm)
            }
        }
    }
}
