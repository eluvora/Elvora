package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class ElvoraDesignTokens(
    val isDark: Boolean,
    val pageBackgroundBrush: Brush,
    val primaryGradient: Brush,
    val accentPinkGradient: Brush,
    val heroBannerBrush: Brush,
    val cardBorderBrush: Brush,
    val navBarContainer: Color,
    val navBarIndicator: Color,
    val notebookPaper: Color,
    val notebookRuleLine: Color,
    val notebookMarginLine: Color,
    val highlighterPink: Color,
    val highlighterPurple: Color,
    val highlighterBlue: Color,
    val flipCardFront: Color,
    val flipCardBack: Color,
    val successColor: Color,
    val successContainer: Color,
    val errorColor: Color,
    val errorContainer: Color,
    val noticeColor: Color,
    val noticeContainer: Color,
    val blueBoxBg: Color,
    val blueBoxBorder: Color,
    val pinkBoxBg: Color,
    val pinkBoxBorder: Color
)

private val LightElvoraTokens = ElvoraDesignTokens(
    isDark = false,
    pageBackgroundBrush = Brush.verticalGradient(
        listOf(LightBackgroundTop, LightBackgroundMid, LightBackgroundBottom)
    ),
    primaryGradient = Brush.linearGradient(listOf(DeepRosePink, VividMagentaPink, MidOceanBlue)),
    accentPinkGradient = Brush.linearGradient(listOf(DarkMagenta, DeepRosePink, SoftPinkGlow)),
    heroBannerBrush = Brush.linearGradient(listOf(DeepRosePink, MidOceanBlue, DeepMidnightBlue)),
    cardBorderBrush = Brush.linearGradient(
        listOf(SoftPinkGlow.copy(alpha = 0.55f), DreamySkyBlue.copy(alpha = 0.55f))
    ),
    navBarContainer = Color(0xFFFAEBF4),
    navBarIndicator = LightBlushPink,
    notebookPaper = NotebookPaperWhite,
    notebookRuleLine = NotebookLineBlue,
    notebookMarginLine = NotebookMarginPink,
    highlighterPink = HighlighterPink,
    highlighterPurple = HighlighterPurple,
    highlighterBlue = HighlighterBlue,
    flipCardFront = IceBlueSurface,
    flipCardBack = LightBlushPink,
    successColor = SuccessEmerald,
    successContainer = SuccessSurface,
    errorColor = ErrorCoral,
    errorContainer = ErrorSurface,
    noticeColor = NoticeRoseBlue,
    noticeContainer = NoticeSurface,
    blueBoxBg = IceBlueSurface,
    blueBoxBorder = MidOceanBlue,
    pinkBoxBg = PalePinkSurface,
    pinkBoxBorder = VividMagentaPink
)

private val DarkElvoraTokens = ElvoraDesignTokens(
    isDark = true,
    pageBackgroundBrush = Brush.verticalGradient(
        listOf(DarkBackgroundTop, DarkBackgroundMid, DarkBackgroundBottom)
    ),
    primaryGradient = Brush.linearGradient(listOf(DarkMagenta, DeepRosePink, MidOceanBlue)),
    accentPinkGradient = Brush.linearGradient(listOf(DarkMagenta, VividMagentaPink, SoftPinkGlow)),
    heroBannerBrush = Brush.linearGradient(listOf(DarkMagenta, DeepRosePink, MidOceanBlue, DeepMidnightBlue)),
    cardBorderBrush = Brush.linearGradient(
        listOf(BlushPinkAccent.copy(alpha = 0.45f), SoftSkyBlue.copy(alpha = 0.45f))
    ),
    navBarContainer = Color(0xFF2A1342),
    navBarIndicator = DarkRoseCardSurface,
    notebookPaper = NotebookPaperWhite, // SmartNotes copy paper remains crisp white
    notebookRuleLine = NotebookLineBlue,
    notebookMarginLine = NotebookMarginPink,
    highlighterPink = HighlighterPink,
    highlighterPurple = HighlighterPurple,
    highlighterBlue = HighlighterBlue,
    flipCardFront = Color(0xFF17296F),
    flipCardBack = Color(0xFF69154B),
    successColor = Color(0xFF34D399),
    successContainer = Color(0xFF064E3B),
    errorColor = Color(0xFFFB7185),
    errorContainer = Color(0xFF881337),
    noticeColor = SoftPinkGlow,
    noticeContainer = DarkPlumPinkSurface,
    blueBoxBg = Color(0xFF18286C),
    blueBoxBorder = SoftSkyBlue,
    pinkBoxBg = DarkPlumPinkSurface,
    pinkBoxBorder = SoftPinkGlow
)

val LocalElvoraTokens = staticCompositionLocalOf { LightElvoraTokens }

private val LightColorScheme = lightColorScheme(
    primary = MidOceanBlue,
    onPrimary = Color.White,
    primaryContainer = PastelSkyBlue,
    onPrimaryContainer = DeepMidnightBlue,
    secondary = DeepRosePink,
    onSecondary = Color.White,
    secondaryContainer = LightBlushPink,
    onSecondaryContainer = DarkMagenta,
    tertiary = DreamySkyBlue,
    onTertiary = DeepMidnightBlue,
    tertiaryContainer = IceBlueSurface,
    onTertiaryContainer = DeepMidnightBlue,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = ErrorCoral,
    onError = Color.White,
    errorContainer = ErrorSurface,
    onErrorContainer = DarkMagenta
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftSkyBlue,
    onPrimary = DeepMidnightBlue,
    primaryContainer = DarkNavyBlue,
    onPrimaryContainer = PastelSkyBlue,
    secondary = SoftPinkGlow,
    onSecondary = DarkMagenta,
    secondaryContainer = DarkRoseCardSurface,
    onSecondaryContainer = LightBlushPink,
    tertiary = MidOceanBlue,
    onTertiary = Color.White,
    tertiaryContainer = DarkNavyBlue,
    onTertiaryContainer = IceBlueSurface,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF881337),
    onErrorContainer = Color(0xFFFFE4E6)
)

val ElvoraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

object ElvoraSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

@Composable
fun ElvoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val tokens = if (darkTheme) DarkElvoraTokens else LightElvoraTokens

    CompositionLocalProvider(LocalElvoraTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ElvoraTypography,
            shapes = ElvoraShapes,
            content = content
        )
    }
}
