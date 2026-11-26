package ru.ngieu.raspisanie.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import ru.ngieu.raspisanie.data.LessonType
import ru.ngieu.raspisanie.data.ThemeModeSetting

enum class AppVisualSkin {
    NEON,
    GLASS,
    AERO,
    TERMINAL,
    PAPER,
    STANDARD,
}

@Immutable
data class AppVisualStyle(
    val skin: AppVisualSkin,
    val isDark: Boolean,
) {
    val isNeon: Boolean get() = skin == AppVisualSkin.NEON
    val isGlass: Boolean get() = skin == AppVisualSkin.GLASS
    val isAero: Boolean get() = skin == AppVisualSkin.AERO
    val isTerminal: Boolean get() = skin == AppVisualSkin.TERMINAL
    val isPaper: Boolean get() = skin == AppVisualSkin.PAPER
    val isTranslucent: Boolean get() =
        skin == AppVisualSkin.NEON ||
            skin == AppVisualSkin.GLASS ||
            skin == AppVisualSkin.AERO ||
            skin == AppVisualSkin.TERMINAL
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

object AeroPalette {
    val backgroundBase = Color(0xFF0A4D8C)

    // Двухполосный глянец Windows 7 Aero Glass: светлый небесный верх -> глубокий сапфировый горизонт -> нижний блик
    val panelFillTop = Color(0xDF338AC0)
    val panelFillMidUpper = Color(0xDF1F72AA)
    val panelFillMidLower = Color(0xE307558D)
    val panelFillBottom = Color(0xE3116CA5)

    val cardFillTop = Color(0xDB3E89B6)
    val cardFillMidUpper = Color(0xDB2A75A5)
    val cardFillMidLower = Color(0xDF115F94)
    val cardFillBottom = Color(0xE010679F)

    val dockFillTop = Color(0xDC3086BB)
    val dockFillMidUpper = Color(0xDC1F70A7)
    val dockFillMidLower = Color(0xE007548A)
    val dockFillBottom = Color(0xE01068A0)

    val dayTileFillTop = Color(0xD43085BB)
    val dayTileFillMidUpper = Color(0xD41E70A6)
    val dayTileFillMidLower = Color(0xDB07548A)
    val dayTileFillBottom = Color(0xDB116AA1)

    // Активные элементы: кнопка «Открыть сегодня», выбранная вкладка, выбранный день
    val activeFillTop = Color(0xF537AEEF)
    val activeFillMidUpper = Color(0xF51387E0)
    val activeFillMidLower = Color(0xF70062CA)
    val activeFillBottom = Color(0xF5019EE5)

    // Стеклянные контуры и внутренний блик
    val borderTop = Color(0xEBC6F1FF)
    val borderMid = Color(0x7A4FAAD9)
    val borderBottom = Color(0xCC9EE0FC)
    val innerContour = Color(0x4DBCEFFE)

    val activeBorderTop = Color(0xFFF5FCFF)
    val activeBorderBottom = Color(0xD9AEE8FF)

    // Плашки типа пары («Практика», «Лекция») — светлое ледяное стекло + тёмный шрифт Aero
    val typePillFillTop = Color(0xF09AD5F4)
    val typePillFillBottom = Color(0xEB7AC1E8)
    val typePillBorder = Color(0xFFF2FBFF)
    val typePillText = Color(0xFF07375D)

    val secondaryPillBg = Color(0x781D6EA6)
    val secondaryPillBorder = Color(0x88BCEFFE)
    val secondaryPillText = Color(0xFFEAF7FF)

    val textPrimary = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFC4E6FF)
    val textMuted = Color(0xFFD8F0FF)
    val iconTint = Color(0xFFFFFFFF)

    // Вертикальная шкала и маркеры времени
    val timelineLineTop = Color(0xFFE2F6FE)
    val timelineLineBottom = Color(0xFFA6E2FE)
    val timelineDot = Color(0xFF2888C8)
    val timelineDotGlow = Color(0x88BCEFFE)
    val timelineActiveNow = Color(0xFF34D399)

    // Светящаяся голубая полоса прогресса Aero
    val progressTrack = Color(0x8200375E)
    val progressFillTop = Color(0xFF99E6FF)
    val progressFillMid = Color(0xFF37CEFF)
    val progressFillBottom = Color(0xFF0078C8)

    // Состояние идущей сейчас пары / замены / отмены в Aero
    val activeLessonTop = Color(0xDE1B8C75)
    val activeLessonMid = Color(0xE00A5B4D)
    val activeLessonBottom = Color(0xE00E7561)
    val activeLessonBorder = Color(0xFF5EEAD4)
    val activeLessonAccent = Color(0xFF34D399)

    val changeLessonTop = Color(0xDC9E6C1A)
    val changeLessonMid = Color(0xDE6B450B)
    val changeLessonBottom = Color(0xDE825510)

    val cancelLessonTop = Color(0xDC982634)
    val cancelLessonMid = Color(0xDE64131E)
    val cancelLessonBottom = Color(0xDE7A1926)

    fun panelFillBrush(): Brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to panelFillTop,
            0.46f to panelFillMidUpper,
            0.50f to panelFillMidLower,
            1.00f to panelFillBottom,
        ),
    )

    fun cardFillBrush(): Brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to cardFillTop,
            0.46f to cardFillMidUpper,
            0.50f to cardFillMidLower,
            1.00f to cardFillBottom,
        ),
    )

    fun dockFillBrush(): Brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to dockFillTop,
            0.46f to dockFillMidUpper,
            0.50f to dockFillMidLower,
            1.00f to dockFillBottom,
        ),
    )

    fun dayTileFillBrush(): Brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to dayTileFillTop,
            0.46f to dayTileFillMidUpper,
            0.50f to dayTileFillMidLower,
            1.00f to dayTileFillBottom,
        ),
    )

    fun activeFillBrush(): Brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to activeFillTop,
            0.46f to activeFillMidUpper,
            0.50f to activeFillMidLower,
            1.00f to activeFillBottom,
        ),
    )

    fun glassBorderBrush(): Brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to borderTop,
            0.50f to borderMid,
            1.00f to borderBottom,
        ),
    )

    fun activeBorderBrush(): Brush = Brush.verticalGradient(
        colors = listOf(activeBorderTop, activeBorderBottom),
    )

    fun progressFillBrush(): Brush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to progressFillTop,
            0.48f to progressFillMid,
            1.00f to progressFillBottom,
        ),
    )
}

object TerminalPalette {
    val backgroundBase = Color(0xFF040B05)
    val vignetteCenter = Color(0xFF0B2410)
    val scanline = Color(0x2E000000)

    // Поверхности панелей и карточек — тёмный фосфорный ЭЛТ-экран
    val panelFillTop = Color(0xE00A1F0D)
    val panelFillBottom = Color(0xEB051107)

    val cardFillTop = Color(0xD90A1C0D)
    val cardFillBottom = Color(0xE6061108)

    val dockFillTop = Color(0xE8071609)
    val dockFillBottom = Color(0xF0040D05)

    val dayTileFillTop = Color(0xCF08180B)
    val dayTileFillBottom = Color(0xDE040D06)

    // Активные состояния: «Открыть сегодня», выбранная вкладка, выбранный день
    val activeFillTop = Color(0xF2174B1B)
    val activeFillBottom = Color(0xF50C2B0F)

    // Контуры HUD
    val borderTop = Color(0xEB57E84A)
    val borderBottom = Color(0xB8329E2B)
    val activeBorderTop = Color(0xFF8CFF78)
    val activeBorderBottom = Color(0xEB4BE33E)
    val divider = Color(0x7050D443)
    val glowGreen = Color(0xFF39FF4C)

    // Бейджи типов занятий и подгрупп
    val typePillBgTop = Color(0xE6113316)
    val typePillBgBottom = Color(0xEB091F0C)
    val typePillBorder = Color(0xFF55DF48)
    val typePillText = Color(0xFF9BFF95)

    val secondaryPillBg = Color(0xAD0D2711)
    val secondaryPillBorder = Color(0x9942B83A)
    val secondaryPillText = Color(0xFF7BE875)

    // Текст и иконки (фосфорный зелёный)
    val textPrimary = Color(0xFF96FF91)
    val textSecondary = Color(0xFF5EC45A)
    val textMuted = Color(0xFF4BA648)
    val iconTint = Color(0xFF84FF7F)

    // Таймлайн и маркер времени
    val timelineLine = Color(0xD950D443)
    val timelineDot = Color(0xFF77FF62)
    val timelineDotGlow = Color(0x6631FF20)
    val timelineActiveNow = Color(0xFFA7FF83)

    // Полоса прогресса дня и пары
    val progressTrack = Color(0xA60E2B12)
    val progressFillStart = Color(0xFF8CFF84)
    val progressFillEnd = Color(0xFF38EB3C)

    // Состояния текущей пары / замены / отмены
    val activeLessonTop = Color(0xEA14441A)
    val activeLessonBottom = Color(0xEE09260D)
    val activeLessonBorder = Color(0xFF8CFF78)
    val activeLessonAccent = Color(0xFF64FF58)

    val changeLessonTop = Color(0xDC302B09)
    val changeLessonBottom = Color(0xE41E1B05)
    val changeBorder = Color(0xFFFACC15)

    val cancelLessonTop = Color(0xDC340E10)
    val cancelLessonBottom = Color(0xE421080A)
    val cancelBorder = Color(0xFFFF5C5C)

    fun panelFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(panelFillTop, panelFillBottom),
    )

    fun cardFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(cardFillTop, cardFillBottom),
    )

    fun dockFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(dockFillTop, dockFillBottom),
    )

    fun dayTileFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(dayTileFillTop, dayTileFillBottom),
    )

    fun activeFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(activeFillTop, activeFillBottom),
    )

    fun glassBorderBrush(): Brush = Brush.verticalGradient(
        colors = listOf(borderTop, borderBottom),
    )

    fun activeBorderBrush(): Brush = Brush.verticalGradient(
        colors = listOf(activeBorderTop, activeBorderBottom),
    )

    fun progressFillBrush(): Brush = Brush.horizontalGradient(
        colors = listOf(progressFillEnd, progressFillStart),
    )
}

object PaperPalette {
    // Тёплый бумажный фон (#F8F6F0 / #E8E5DC) и графитовые чернила (#171815 / #2D2E2A)
    val backgroundBase = Color(0xFFF3F0E6)
    val surfacePaper = Color(0xFFF8F6F0)
    val surfaceElevated = Color(0xFFFAF8F3)
    val surfaceMuted = Color(0xFFEBE7DC)

    // Тонкая чернильная обводка и разделители (21, 31, 32, 39, 46, 49, 65, 67, 82)
    val borderInk = Color(0xDB181915)
    val borderSubtle = Color(0x52181915)
    val divider = Color(0x36171815)
    val headerDivider = Color(0x66171815)

    // Активная подложка: глубокий тёплый графит #2D2E2A + кремовый текст #EFEDE1 (22, 24, 50, 66, 81)
    val activeFill = Color(0xFF2D2E2A)
    val activeText = Color(0xFFEFEDE1)
    val secondaryPillBg = Color(0xFFE9E6DC)
    val secondaryPillBorder = Color(0x99181915)

    // Кнопка сворачивания сводки дня (38 · Свернуть — круглая подложка)
    val chevronCircleBg = Color(0x241C1C1C)

    // Чернила текста и иконок
    val textPrimary = Color(0xFF171815)
    val textSecondary = Color(0xFF5A5B54)
    val textMuted = Color(0xFF777870)
    val iconTint = Color(0xFF171815)

    // Таймлайн и маркер времени (47, 48)
    val timelineLine = Color(0xCC171815)
    val timelineDot = Color(0xFF171815)
    val timelineActiveNow = Color(0xFF1F6E4A)

    // Полоса прогресса (59 · Карточка — линия прогресса)
    val progressTrack = Color(0x26171815)
    val progressFill = Color(0xFF171815)

    // Семантические состояния в бумажной палитре
    val activeLessonBg = Color(0xFFEAF2EC)
    val activeLessonBorder = Color(0xFF1F6E4A)
    val changeLessonBg = Color(0xFFF7EFE0)
    val changeLessonBorder = Color(0xFFB46914)
    val cancelLessonBg = Color(0xFFF7E9E8)
    val cancelLessonBorder = Color(0xFFB33939)

    fun panelFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(surfacePaper, surfacePaper),
    )

    fun cardFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(surfaceElevated, surfacePaper),
    )

    fun dockFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(surfacePaper, surfacePaper),
    )

    fun activeFillBrush(): Brush = Brush.verticalGradient(
        colors = listOf(activeFill, activeFill),
    )

    fun borderBrush(): Brush = Brush.verticalGradient(
        colors = listOf(borderInk, borderInk),
    )

    fun progressFillBrush(): Brush = Brush.horizontalGradient(
        colors = listOf(progressFill, progressFill),
    )
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

private val AeroColors = darkColorScheme(
    primary = Color(0xFF37CEFF),
    onPrimary = Color(0xFF032544),
    primaryContainer = Color(0xFF0A5E9E),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF83C4E9),
    onSecondary = Color(0xFF07375D),
    secondaryContainer = Color(0xFF0C4A7A),
    onSecondaryContainer = Color(0xFFEAF7FF),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF0B5D4D),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = AeroPalette.backgroundBase,
    onBackground = AeroPalette.textPrimary,
    surface = Color(0xFF0F568D),
    onSurface = AeroPalette.textPrimary,
    surfaceVariant = Color(0xFF176BA8),
    onSurfaceVariant = AeroPalette.textSecondary,
    outline = Color(0xFF7EC9EE),
    outlineVariant = Color(0x52BCEFFE),
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF64131E),
    onErrorContainer = Color(0xFFFFE4E6),
)

private val TerminalColors = darkColorScheme(
    primary = Color(0xFF64FF58),
    onPrimary = Color(0xFF031505),
    primaryContainer = Color(0xFF123C16),
    onPrimaryContainer = TerminalPalette.textPrimary,
    secondary = Color(0xFF5EC45A),
    onSecondary = Color(0xFF041706),
    secondaryContainer = Color(0xFF0D2810),
    onSecondaryContainer = TerminalPalette.textPrimary,
    tertiary = Color(0xFFA7FF83),
    onTertiary = Color(0xFF031A05),
    tertiaryContainer = Color(0xFF14441A),
    onTertiaryContainer = Color(0xFFD9FFD2),
    background = TerminalPalette.backgroundBase,
    onBackground = TerminalPalette.textPrimary,
    surface = Color(0xFF081A0B),
    onSurface = TerminalPalette.textPrimary,
    surfaceVariant = Color(0xFF0F2B13),
    onSurfaceVariant = TerminalPalette.textSecondary,
    outline = Color(0xFF50D443),
    outlineVariant = Color(0x6650D443),
    error = Color(0xFFFF5C5C),
    onError = Color(0xFF2B0505),
    errorContainer = Color(0xFF340E10),
    onErrorContainer = Color(0xFFFFD6D6),
)

private val PaperColors = lightColorScheme(
    primary = PaperPalette.activeFill,
    onPrimary = PaperPalette.activeText,
    primaryContainer = PaperPalette.surfaceMuted,
    onPrimaryContainer = PaperPalette.textPrimary,
    secondary = PaperPalette.textSecondary,
    onSecondary = PaperPalette.activeText,
    secondaryContainer = PaperPalette.secondaryPillBg,
    onSecondaryContainer = PaperPalette.textPrimary,
    tertiary = PaperPalette.timelineActiveNow,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = PaperPalette.activeLessonBg,
    onTertiaryContainer = PaperPalette.textPrimary,
    background = PaperPalette.backgroundBase,
    onBackground = PaperPalette.textPrimary,
    surface = PaperPalette.surfacePaper,
    onSurface = PaperPalette.textPrimary,
    surfaceVariant = PaperPalette.surfaceMuted,
    onSurfaceVariant = PaperPalette.textSecondary,
    outline = PaperPalette.borderInk,
    outlineVariant = PaperPalette.divider,
    error = PaperPalette.cancelLessonBorder,
    onError = Color(0xFFFFFFFF),
    errorContainer = PaperPalette.cancelLessonBg,
    onErrorContainer = Color(0xFF5C1919),
)

private val DefaultTypography = Typography()
private val PaperTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = FontFamily.Serif),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = FontFamily.Serif),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = FontFamily.Serif),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = FontFamily.Serif),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = FontFamily.Serif),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = FontFamily.Serif),
    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = FontFamily.Serif),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = FontFamily.Serif),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = FontFamily.Serif),
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
        ThemeModeSetting.AERO -> AppVisualSkin.AERO
        ThemeModeSetting.TERMINAL -> AppVisualSkin.TERMINAL
        ThemeModeSetting.PAPER -> AppVisualSkin.PAPER
        ThemeModeSetting.SYSTEM -> if (systemDark) AppVisualSkin.NEON else AppVisualSkin.STANDARD
        ThemeModeSetting.LIGHT -> AppVisualSkin.STANDARD
        ThemeModeSetting.DARK -> AppVisualSkin.STANDARD
    }
    val isDark = when (themeMode) {
        ThemeModeSetting.NEON, ThemeModeSetting.GLASS, ThemeModeSetting.AERO, ThemeModeSetting.TERMINAL -> true
        ThemeModeSetting.SYSTEM -> systemDark
        ThemeModeSetting.PAPER, ThemeModeSetting.LIGHT -> false
        ThemeModeSetting.DARK -> true
    }

    val colorScheme = when {
        skin == AppVisualSkin.NEON -> NeonColors
        skin == AppVisualSkin.GLASS -> GlassColors
        skin == AppVisualSkin.AERO -> AeroColors
        skin == AppVisualSkin.TERMINAL -> TerminalColors
        skin == AppVisualSkin.PAPER -> PaperColors
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
            typography = if (skin == AppVisualSkin.PAPER) PaperTypography else DefaultTypography,
            content = content,
        )
    }
}
