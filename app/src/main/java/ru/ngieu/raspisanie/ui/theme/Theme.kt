package ru.ngieu.raspisanie.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import ru.ngieu.raspisanie.data.LessonType
import ru.ngieu.raspisanie.data.ThemeModeSetting

enum class AppVisualSkin {
    NEON,
    GLASS,
    STANDARD,
}

@Immutable
data class AppVisualStyle(
    val skin: AppVisualSkin,
    val isDark: Boolean,
) {
    val isNeon: Boolean get() = skin == AppVisualSkin.NEON
    val isGlass: Boolean get() = skin == AppVisualSkin.GLASS
    val isTranslucent: Boolean get() = skin == AppVisualSkin.NEON || skin == AppVisualSkin.GLASS
}

val LocalAppVisualStyle = staticCompositionLocalOf {
    AppVisualStyle(skin = AppVisualSkin.NEON, isDark = true)
}

object NeonPalette {
    val backgroundBase = Color(0xFF060916)
    val leftEdgeGlow = Color(0xFF8B5CF6)
    val rightEdgeGlow = Color(0xFF2563EB)
    val rightCyanGlow = Color(0xFF0EA5E9)

    val glassSurface = Color(0x9E141B38)
    val glassSurfaceElevated = Color(0xB81B244A)
    val glassBorderTop = Color(0x3DFFFFFF)
    val glassBorderBottom = Color(0x1F818CF8)

    val activeDayStart = Color(0xFF9D4EDD)
    val activeDayEnd = Color(0xFF5B46F6)
    val activeDayBorder = Color(0xFFE9D5FF)

    val activeLessonStart = Color(0xCC0D4A3E)
    val activeLessonEnd = Color(0xBA093334)
    val activeLessonBorder = Color(0xFF34D399)
    val activeLessonAccent = Color(0xFF10B981)

    val progressStart = Color(0xFFA855F7)
    val progressEnd = Color(0xFF38BDF8)

    val textPrimary = Color(0xFFF5F1FF)
    val textSecondary = Color(0xFFA5B0D6)
    val timelinePurple = Color(0xFF8B5CF6)
    val timelineCyan = Color(0xFF38BDF8)
    val timelineActiveNow = Color(0xFF34D399)
}

object GlassPalette {
    val backgroundBase = Color(0xFF111111)

    // Верхняя навигация, кнопки заголовка, сводка дня (017, 011, 027)
    val panelFillTop = Color(0x8C737373)
    val panelFillBottom = Color(0x782C2C2C)
    val panelBorderTop = Color(0xB8F5F5F5)
    val panelBorderBottom = Color(0x40919191)

    // Активная вкладка / выбранный переключатель (018)
    val activeTabFillTop = Color(0x8CAAAAAA)
    val activeTabFillBottom = Color(0x786E6E6E)

    // Карточка занятия (035, 051)
    val cardFillTop = Color(0x858A8A8A)
    val cardFillMid = Color(0x8C272727)
    val cardFillBottom = Color(0x508A8A8A)
    val cardBorderTop = Color(0xB3F4F4F4)
    val cardBorderMid = Color(0x339B9B9B)
    val cardBorderBottom = Color(0x6BF4F4F4)

    // Нижний календарь недели (071, 072, 076)
    val dockFillTop = Color(0xA19D9D9D)
    val dockFillBottom = Color(0xB8464646)
    val dockBorderTop = Color(0xC2F4F4F4)
    val dockBorderBottom = Color(0x528C8C8C)

    val dayTileFillTop = Color(0x9E4A4A4A)
    val dayTileFillBottom = Color(0x991E1E1E)
    val dayTileBorderTop = Color(0x2EF4F4F4)
    val dayTileBorderBottom = Color(0x1F6E6E6E)

    val daySelectedFillTop = Color(0xC4C0C0C0)
    val daySelectedFillBottom = Color(0xB38A8A8A)
    val daySelectedBorderTop = Color(0xF2F4F4F4)
    val daySelectedBorderBottom = Color(0x996E6E6E)

    // Плашки, текст, таймлайн (036, 038, 034, 047, 050)
    val typePillBg = Color(0x85BFBFBF)
    val typePillText = Color(0xFFF8F8F8)
    val secondaryPillBg = Color(0x61959595)
    val secondaryPillText = Color(0xFFCDCDCD)

    val textPrimary = Color(0xFFFAFAFA)
    val textSecondary = Color(0xFFC1C1C1)
    val textMuted = Color(0xFFC9C9C9)
    val iconTint = Color(0xFFDDDDDD)

    val timelineLine = Color(0xB39D9D9D)
    val timelineDot = Color(0xFFCFCFCF)
    val timelineActiveNow = Color(0xFF34D399)

    val progressTrack = Color(0x5EB2B2B2)
    val progressFillStart = Color(0xFFF4F4F4)
    val progressFillEnd = Color(0xFFBFBFBF)

    val activeLessonStart = Color(0x9E445A52)
    val activeLessonEnd = Color(0x94212B27)
    val activeLessonBorder = Color(0xFF6EE7B7)
    val activeLessonAccent = Color(0xFF34D399)
}

private val NeonColors = darkColorScheme(
    primary = Color(0xFF9333EA),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF261F4D),
    onPrimaryContainer = Color(0xFFF5F1FF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF041C2C),
    secondaryContainer = Color(0xFF162244),
    onSecondaryContainer = Color(0xFFE2E8F0),
    tertiary = Color(0xFF10B981),
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = NeonPalette.backgroundBase,
    onBackground = NeonPalette.textPrimary,
    surface = Color(0xFF141B38),
    onSurface = NeonPalette.textPrimary,
    surfaceVariant = Color(0xFF1C2447),
    onSurfaceVariant = NeonPalette.textSecondary,
    outline = Color(0xFF3B4675),
    outlineVariant = Color(0x28FFFFFF),
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF4C1124),
    onErrorContainer = Color(0xFFFFE4E6),
)

private val GlassColors = darkColorScheme(
    primary = Color(0xFFE5E5E5),
    onPrimary = Color(0xFF141414),
    primaryContainer = Color(0xFF424242),
    onPrimaryContainer = Color(0xFFFAFAFA),
    secondary = Color(0xFFC2C2C2),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF2C2C2C),
    onSecondaryContainer = Color(0xFFF5F5F5),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF1E2D28),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = GlassPalette.backgroundBase,
    onBackground = GlassPalette.textPrimary,
    surface = Color(0xFF222222),
    onSurface = GlassPalette.textPrimary,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = GlassPalette.textSecondary,
    outline = Color(0xFF737373),
    outlineVariant = Color(0x38F4F4F4),
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF4A1922),
    onErrorContainer = Color(0xFFFFE4E6),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF155EEF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD9E6FF),
    onPrimaryContainer = Color(0xFF001B4E),
    secondary = Color(0xFF475467),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE9EEF5),
    onSecondaryContainer = Color(0xFF101828),
    tertiary = Color(0xFF0E9384),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCCFBEF),
    onTertiaryContainer = Color(0xFF053321),
    background = Color(0xFFF6F8FC),
    onBackground = Color(0xFF101828),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101828),
    surfaceVariant = Color(0xFFEEF2F8),
    onSurfaceVariant = Color(0xFF475467),
    outline = Color(0xFFD0D5DD),
    outlineVariant = Color(0xFFE4E7EC),
    error = Color(0xFFD92D20),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = Color(0xFF7A271A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF84ADFF),
    onPrimary = Color(0xFF002D75),
    primaryContainer = Color(0xFF0043A8),
    onPrimaryContainer = Color(0xFFD9E6FF),
    secondary = Color(0xFFB2BDCD),
    onSecondary = Color(0xFF1D2939),
    secondaryContainer = Color(0xFF283244),
    onSecondaryContainer = Color(0xFFE4E7EC),
    tertiary = Color(0xFF5FE9D0),
    onTertiary = Color(0xFF00382F),
    tertiaryContainer = Color(0xFF0A4F45),
    onTertiaryContainer = Color(0xFFCCFBEF),
    background = Color(0xFF0D111A),
    onBackground = Color(0xFFF2F4F7),
    surface = Color(0xFF151B26),
    onSurface = Color(0xFFF2F4F7),
    surfaceVariant = Color(0xFF1E2636),
    onSurfaceVariant = Color(0xFF98A2B3),
    outline = Color(0xFF344054),
    outlineVariant = Color(0xFF242D3D),
    error = Color(0xFFF97066),
    onError = Color(0xFF550D08),
    errorContainer = Color(0xFF7A271A),
    onErrorContainer = Color(0xFFFEE4E2),
)

object ScheduleAccentColors {
    val changeBgLight = Color(0xFFFFF4E5)
    val changeBorderLight = Color(0xFFF79009)
    val changeBadgeLight = Color(0xFFDC6803)

    val changeBgDark = Color(0xFF151B26)
    val changeBorderDark = Color(0xFF925812)
    val changeBadgeDark = Color(0xFFB87424)
    val changeBadgeBgDark = Color(0x523B2409)

    val cancelBgLight = Color(0xFFFEF2F2)
    val cancelBorderLight = Color(0xFFEF4444)
    val cancelBadgeBgLight = Color(0xFFFEE2E2)
    val cancelBadgeLight = Color(0xFFDC2626)

    val cancelBgDark = Color(0x85261013)
    val cancelBorderDark = Color(0xFF8C2D36)
    val cancelBadgeBgDark = Color(0x5245151B)
    val cancelBadgeDark = Color(0xFFD46A72)

    val cancelBgNeonStart = Color(0xBD3B1221)
    val cancelBgNeonEnd = Color(0xB3240C1A)
    val cancelBorderNeon = Color(0xFFF87171)
    val cancelBadgeBgNeon = Color(0x47EF4444)
    val cancelBadgeNeon = Color(0xFFFCA5A5)

    val cancelBgGlassStart = Color(0xA64B1B22)
    val cancelBgGlassEnd = Color(0x94291014)

    val currentLessonBgLight = Color(0xFFE6F9F2)
    val currentLessonBorderLight = Color(0xFF059669)
    val currentLessonBgDark = Color(0x800B3B2E)
    val currentLessonBorderDark = Color(0xFF34D399)

    fun lessonTypeColor(type: LessonType, isDark: Boolean): Color {
        return if (isDark) Color(0xFF94A3B8) else Color(0xFF475467)
    }
}

@Composable
fun NgieuTheme(
    themeMode: ThemeModeSetting,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val skin = when (themeMode) {
        ThemeModeSetting.NEON -> AppVisualSkin.NEON
        ThemeModeSetting.GLASS -> AppVisualSkin.GLASS
        ThemeModeSetting.SYSTEM -> if (systemDark) AppVisualSkin.NEON else AppVisualSkin.STANDARD
        ThemeModeSetting.LIGHT -> AppVisualSkin.STANDARD
        ThemeModeSetting.DARK -> AppVisualSkin.STANDARD
    }
    val isDark = when (themeMode) {
        ThemeModeSetting.NEON, ThemeModeSetting.GLASS -> true
        ThemeModeSetting.SYSTEM -> systemDark
        ThemeModeSetting.LIGHT -> false
        ThemeModeSetting.DARK -> true
    }

    val colorScheme = when {
        skin == AppVisualSkin.NEON -> NeonColors
        skin == AppVisualSkin.GLASS -> GlassColors
        isDark -> DarkColors
        else -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalAppVisualStyle provides AppVisualStyle(skin = skin, isDark = isDark),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
