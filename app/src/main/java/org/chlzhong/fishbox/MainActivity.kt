package org.chlzhong.fishbox

import android.os.Build
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
        requestHighRefreshRate()

        val font = loadAppFont(this)
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
                    // 手势导航的小白条行也完全透明（不要白色/灰色遮罩），鱼卡片直接滑到它下面；
                    // 小白条自己的深浅由系统按下面内容的亮度自动调整
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { dark },
                )
                // 关掉系统给导航栏加的对比度遮罩（三键导航时它会把底栏染成半透明白/灰）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = false
                }
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

    /**
     * 请求设备支持的最高刷新率（同分辨率下）。很多手机在"自适应刷新率"下会把普通应用压在 60Hz，
     * 滑动图鉴和详情卡片时更容易看出不流畅；这里只是向系统提出偏好，系统可以因省电/温控而不采纳。
     */
    private fun requestHighRefreshRate() {
        runCatching {
            @Suppress("DEPRECATION")
            val disp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) this.display else windowManager.defaultDisplay
            val current = disp?.mode ?: return
            val best = disp.supportedModes
                .filter { it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight }
                .maxByOrNull { it.refreshRate } ?: return
            if (best.refreshRate > current.refreshRate) {
                val attrs = window.attributes
                attrs.preferredDisplayModeId = best.modeId
                window.attributes = attrs
            }
        }
    }
}
