package ru.ngieu.raspisanie.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ru.ngieu.raspisanie.data.LessonType
import ru.ngieu.raspisanie.data.ThemeModeSetting

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

    val changeBgDark = Color(0xFF33200B)
    val changeBorderDark = Color(0xFFFDB022)
    val changeBadgeDark = Color(0xFFFDB022)

    fun lessonTypeColor(type: LessonType, isDark: Boolean): Color {
        return when (type) {
            LessonType.LECTURE -> if (isDark) Color(0xFF84ADFF) else Color(0xFF155EEF)
            LessonType.PRACTICE -> if (isDark) Color(0xFF5FE9D0) else Color(0xFF0E9384)
            LessonType.LAB -> if (isDark) Color(0xFFB692F6) else Color(0xFF6938EF)
            LessonType.EXAM -> if (isDark) Color(0xFFF97066) else Color(0xFFD92D20)
            LessonType.CONSULTATION -> if (isDark) Color(0xFFFDB022) else Color(0xFFDC6803)
            LessonType.EVENT -> if (isDark) Color(0xFFF472B6) else Color(0xFFC11574)
            LessonType.OTHER -> if (isDark) Color(0xFF98A2B3) else Color(0xFF475467)
        }
    }
}

@Composable
fun NgieuTheme(
    themeMode: ThemeModeSetting,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeModeSetting.SYSTEM -> isSystemInDarkTheme()
        ThemeModeSetting.LIGHT -> false
        ThemeModeSetting.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
