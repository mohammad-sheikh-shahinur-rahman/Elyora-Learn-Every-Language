package com.itamadersomajinc.elyora

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class AccentPair(
    val container: Color,
    val content: Color,
    val badgeBg: Color
)

@Immutable
data class ElyoraAccents(
    val lavender: AccentPair,
    val sky: AccentPair,
    val mint: AccentPair,
    val coral: AccentPair,
    val amber: AccentPair,
    val heroStart: Color,
    val heroMid: Color,
    val heroEnd: Color,
    val heroContent: Color,
    val ctaStart: Color,
    val ctaEnd: Color,
    val pinkRing: Color,
    val tealHighlight: Color,
    val cardBorder: Color,
    val isDark: Boolean
)

private val LightAccents = ElyoraAccents(
    lavender = AccentPair(Color(0xFFEEE9FF), Color(0xFF3E2696), Color(0xFF7B52F2)),
    sky = AccentPair(Color(0xFFE3F4FF), Color(0xFF0A4D73), Color(0xFF3A9DF8)),
    mint = AccentPair(Color(0xFFDFF8F3), Color(0xFF095C52), Color(0xFF19B5A6)),
    coral = AccentPair(Color(0xFFFFE5F1), Color(0xFF8C1E59), Color(0xFFDE5FB7)),
    amber = AccentPair(Color(0xFFFFF3D4), Color(0xFF6B4700), Color(0xFFF5B236)),
    heroStart = Color(0xFFB598FF),
    heroMid = Color(0xFFD6C5FF),
    heroEnd = Color(0xFFF7F5FC),
    heroContent = Color.White,
    ctaStart = Color(0xFF8C52FF),
    ctaEnd = Color(0xFF7045E8),
    pinkRing = Color(0xFFDE5FB7),
    tealHighlight = Color(0xFF15A99B),
    cardBorder = Color(0xFFEBE6F7),
    isDark = false
)

private val DarkAccents = ElyoraAccents(
    lavender = AccentPair(Color(0xFF28214A), Color(0xFFDCD4FF), Color(0xFF987EFF)),
    sky = AccentPair(Color(0xFF153447), Color(0xFFB5E7FF), Color(0xFF4CC6FF)),
    mint = AccentPair(Color(0xFF123B38), Color(0xFFA7F7EE), Color(0xFF39D9C8)),
    coral = AccentPair(Color(0xFF421F35), Color(0xFFFFC8E6), Color(0xFFFF79C6)),
    amber = AccentPair(Color(0xFF3E3012), Color(0xFFFFE59E), Color(0xFFF8C146)),
    heroStart = Color(0xFF473096),
    heroMid = Color(0xFF271F52),
    heroEnd = Color(0xFF0E1019),
    heroContent = Color.White,
    ctaStart = Color(0xFF835BFF),
    ctaEnd = Color(0xFF6340DB),
    pinkRing = Color(0xFFFF79C6),
    tealHighlight = Color(0xFF39D9C8),
    cardBorder = Color(0xFF2B2E45),
    isDark = true
)

val LocalElyoraAccents = staticCompositionLocalOf { LightAccents }

private val ElyoraFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold),
    Font(R.font.noto_sans_bengali, FontWeight.Normal),
    Font(R.font.noto_sans_bengali, FontWeight.Bold)
)

private val ElyoraTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 31.sp,
        lineHeight = 37.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ElyoraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

private val ElyoraShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

private val Light = lightColorScheme(
    primary = Color(0xFF7B52F2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEE9FF),
    onPrimaryContainer = Color(0xFF23116B),
    secondary = Color(0xFFDE5FB7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE5F4),
    onSecondaryContainer = Color(0xFF5A0E42),
    tertiary = Color(0xFF15A99B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDF8F4),
    onTertiaryContainer = Color(0xFF06463F),
    background = Color(0xFFF7F5FC),
    onBackground = Color(0xFF171625),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF171625),
    surfaceVariant = Color(0xFFEDEAF8),
    onSurfaceVariant = Color(0xFF605E78),
    outlineVariant = Color(0xFFE3DDF3),
    error = Color(0xFFD93E4A),
    errorContainer = Color(0xFFFFE5E7),
    onErrorContainer = Color(0xFF680C16)
)

private val Dark = darkColorScheme(
    primary = Color(0xFFA68BFF),
    onPrimary = Color(0xFF190E57),
    primaryContainer = Color(0xFF2B2259),
    onPrimaryContainer = Color(0xFFE8E2FF),
    secondary = Color(0xFFFF79C6),
    onSecondary = Color(0xFF470831),
    secondaryContainer = Color(0xFF3E1C36),
    onSecondaryContainer = Color(0xFFFFD6EF),
    tertiary = Color(0xFF39D9C8),
    onTertiary = Color(0xFF003832),
    tertiaryContainer = Color(0xFF113B37),
    onTertiaryContainer = Color(0xFFB7FAF2),
    background = Color(0xFF0E1019),
    onBackground = Color(0xFFECECF6),
    surface = Color(0xFF181B29),
    onSurface = Color(0xFFECECF6),
    surfaceVariant = Color(0xFF25283D),
    onSurfaceVariant = Color(0xFFB5B7CF),
    outlineVariant = Color(0xFF2E324B),
    error = Color(0xFFFF7A85),
    errorContainer = Color(0xFF4C1C24),
    onErrorContainer = Color(0xFFFFD9DC)
)

@Composable
fun ElyoraTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val accents = if (isDark) DarkAccents else LightAccents
    CompositionLocalProvider(LocalElyoraAccents provides accents) {
        MaterialTheme(
            colorScheme = if (isDark) Dark else Light,
            typography = ElyoraTypography,
            shapes = ElyoraShapes,
            content = content
        )
    }
}
