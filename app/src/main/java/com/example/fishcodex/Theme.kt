package com.example.fishcodex

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

/** 静态配色（Android 11 及以下、或关闭动态取色时使用）。以图标的棕色/米黄色为基调，手工调配的 M3 色板。 */
private val LightColors = lightColorScheme(
    primary = Color(0xFF8C5000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCBD),
    onPrimaryContainer = Color(0xFF2C1600),
    secondary = Color(0xFF725A42),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFDDCBE),
    onSecondaryContainer = Color(0xFF291806),
    tertiary = Color(0xFF5B6236),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0E8B0),
    onTertiaryContainer = Color(0xFF191E00),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFF8F4),
    onBackground = Color(0xFF201A15),
    surface = Color(0xFFFFF8F4),
    onSurface = Color(0xFF201A15),
    surfaceVariant = Color(0xFFF2DFD0),
    onSurfaceVariant = Color(0xFF51443A),
    outline = Color(0xFF84746A),
    outlineVariant = Color(0xFFD6C3B7),
    inverseSurface = Color(0xFF362F29),
    inverseOnSurface = Color(0xFFFBEEE5),
    inversePrimary = Color(0xFFFFB86F),
    surfaceTint = Color(0xFF8C5000),
    surfaceDim = Color(0xFFE4D8CE),
    surfaceBright = Color(0xFFFFF8F4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1E8),
    surfaceContainer = Color(0xFFFAEBE0),
    surfaceContainerHigh = Color(0xFFF4E5DA),
    surfaceContainerHighest = Color(0xFFEEE0D5),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB86F),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6A3B00),
    onPrimaryContainer = Color(0xFFFFDCBD),
    secondary = Color(0xFFE0C1A3),
    onSecondary = Color(0xFF402C17),
    secondaryContainer = Color(0xFF58422B),
    onSecondaryContainer = Color(0xFFFDDCBE),
    tertiary = Color(0xFFC4CC96),
    onTertiary = Color(0xFF2D3407),
    tertiaryContainer = Color(0xFF43491B),
    onTertiaryContainer = Color(0xFFE0E8B0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF18120C),
    onBackground = Color(0xFFEDE0D6),
    surface = Color(0xFF18120C),
    onSurface = Color(0xFFEDE0D6),
    surfaceVariant = Color(0xFF51443A),
    onSurfaceVariant = Color(0xFFD6C3B7),
    outline = Color(0xFF9F8E82),
    outlineVariant = Color(0xFF51443A),
    inverseSurface = Color(0xFFEDE0D6),
    inverseOnSurface = Color(0xFF362F29),
    inversePrimary = Color(0xFF8C5000),
    surfaceTint = Color(0xFFFFB86F),
    surfaceDim = Color(0xFF18120C),
    surfaceBright = Color(0xFF3F3731),
    surfaceContainerLowest = Color(0xFF130D07),
    surfaceContainerLow = Color(0xFF211A14),
    surfaceContainer = Color(0xFF251E18),
    surfaceContainerHigh = Color(0xFF302922),
    surfaceContainerHighest = Color(0xFF3B342D),
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
 * 全应用唯一的主题入口：Android 12+ 且开启动态取色时使用系统壁纸配色，否则使用静态配色。
 * 业务页面一律通过 MaterialTheme.colorScheme 取色。
 * 唯一的例外是"品质"语义色（QUALITY_COLORS，灰/绿/蓝/紫/金），它表示稀有度，不随主题变化。
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
