package org.chlzhong.fishbox

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Material 3 amber / yellow-orange palette used by default. */
private val LightColors = lightColorScheme(
    primary = Color(0xFF815500),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDEAA),
    onPrimaryContainer = Color(0xFF291800),
    inversePrimary = Color(0xFFD3A45C),
    secondary = Color(0xFF715B40),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF6DFC0),
    onSecondaryContainer = Color(0xFF281A06),
    tertiary = Color(0xFF86502D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCBD),
    onTertiaryContainer = Color(0xFF2F1600),
    background = Color(0xFFFFFBF5),
    onBackground = Color(0xFF201B13),
    surface = Color(0xFFFFFBF5),
    onSurface = Color(0xFF201B13),
    surfaceVariant = Color(0xFFF1E0C8),
    onSurfaceVariant = Color(0xFF4F4537),
    surfaceTint = Color(0xFF815500),
    inverseSurface = Color(0xFF352F25),
    inverseOnSurface = Color(0xFFF8EFE2),
    outline = Color(0xFF817562),
    outlineVariant = Color(0xFFD2C4AC),
    scrim = Color(0xFF000000),
    surfaceDim = Color(0xFFDED5C9),
    surfaceBright = Color(0xFFFFFBF5),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F1E6),
    surfaceContainer = Color(0xFFF2E9DC),
    surfaceContainerHigh = Color(0xFFECE2D3),
    surfaceContainerHighest = Color(0xFFE5DAC9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB951),
    onPrimary = Color(0xFF452B00),
    primaryContainer = Color(0xFF654000),
    onPrimaryContainer = Color(0xFFFFDEAB),
    inversePrimary = Color(0xFF815500),
    secondary = Color(0xFFDFC29B),
    onSecondary = Color(0xFF3F2D16),
    secondaryContainer = Color(0xFF57432B),
    onSecondaryContainer = Color(0xFFFCE0BA),
    tertiary = Color(0xFFE7BE9D),
    onTertiary = Color(0xFF442A17),
    tertiaryContainer = Color(0xFF5D412D),
    onTertiaryContainer = Color(0xFFFFDBBE),
    background = Color(0xFF17130D),
    onBackground = Color(0xFFEDE1D0),
    surface = Color(0xFF17130D),
    onSurface = Color(0xFFEDE1D0),
    surfaceVariant = Color(0xFF4C4335),
    onSurfaceVariant = Color(0xFFD1C3AD),
    surfaceTint = Color(0xFFFFB951),
    inverseSurface = Color(0xFFEDE1D0),
    inverseOnSurface = Color(0xFF352F25),
    outline = Color(0xFF9A8E7B),
    outlineVariant = Color(0xFF4C4335),
    scrim = Color(0xFF000000),
    surfaceDim = Color(0xFF17130D),
    surfaceBright = Color(0xFF40382C),
    surfaceContainerLowest = Color(0xFF110D08),
    surfaceContainerLow = Color(0xFF201A11),
    surfaceContainer = Color(0xFF251F16),
    surfaceContainerHigh = Color(0xFF30291F),
    surfaceContainerHighest = Color(0xFF3B3328),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private fun appTypography(f: FontFamily?): Typography {
    val d = Typography()
    fun TextStyle.ff(): TextStyle = if (f != null) copy(fontFamily = f) else this
    return Typography(
        displayLarge = d.displayLarge.ff(),
        displayMedium = d.displayMedium.ff(),
        displaySmall = d.displaySmall.copy(fontWeight = FontWeight.SemiBold).ff(),
        headlineLarge = d.headlineLarge.ff(),
        headlineMedium = d.headlineMedium.ff(),
        headlineSmall = d.headlineSmall.ff(),
        titleLarge = d.titleLarge.copy(fontWeight = FontWeight.SemiBold).ff(),
        titleMedium = d.titleMedium.copy(fontWeight = FontWeight.SemiBold).ff(),
        titleSmall = d.titleSmall.ff(),
        bodyLarge = d.bodyLarge.ff(),
        bodyMedium = d.bodyMedium.ff(),
        bodySmall = d.bodySmall.ff(),
        labelLarge = d.labelLarge.ff(),
        labelMedium = d.labelMedium.ff(),
        labelSmall = d.labelSmall.ff(),
    )
}

@Composable
fun isDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/** 等宽数字：进度、价格等数字滚动/刷新时不会左右抖动 */
val TabularNums = TextStyle(fontFeatureSettings = "tnum")

val DYNAMIC_COLOR_SUPPORTED: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * 全应用唯一的主题入口：默认使用黄色 / 橙色的 Material 3 配色；
 * Android 12+ 且用户在设置中开启动态取色时，才切换为系统壁纸配色。
 * 品质语义色（QUALITY_COLORS）表示稀有度，不随主题变化。
 */
@Composable
fun FishTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    fontFamily: FontFamily?,
    content: @Composable () -> Unit,
) {
    val dark = isDarkTheme(themeMode)
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && DYNAMIC_COLOR_SUPPORTED ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = appTypography(fontFamily),
        shapes = AppShapes,
        content = content,
    )
}
