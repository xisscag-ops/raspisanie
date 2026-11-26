package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.ngieu.raspisanie.data.AppIconStyle
import ru.ngieu.raspisanie.data.ResolvedAppIcon
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.ui.AppIconManager

class AppIconManagerTest {

    @Test
    fun `auto icon style matches each explicit theme`() {
        assertEquals(
            ResolvedAppIcon.NEON,
            AppIconManager.resolveIcon(ThemeModeSetting.NEON, AppIconStyle.AUTO, isSystemDark = false),
        )
        assertEquals(
            ResolvedAppIcon.GLASS,
            AppIconManager.resolveIcon(ThemeModeSetting.GLASS, AppIconStyle.AUTO, isSystemDark = false),
        )
        assertEquals(
            ResolvedAppIcon.AERO,
            AppIconManager.resolveIcon(ThemeModeSetting.AERO, AppIconStyle.AUTO, isSystemDark = false),
        )
        assertEquals(
            ResolvedAppIcon.TERMINAL,
            AppIconManager.resolveIcon(ThemeModeSetting.TERMINAL, AppIconStyle.AUTO, isSystemDark = false),
        )
        assertEquals(
            ResolvedAppIcon.LIGHT,
            AppIconManager.resolveIcon(ThemeModeSetting.LIGHT, AppIconStyle.AUTO, isSystemDark = true),
        )
        assertEquals(
            ResolvedAppIcon.DARK,
            AppIconManager.resolveIcon(ThemeModeSetting.DARK, AppIconStyle.AUTO, isSystemDark = false),
        )
    }

    @Test
    fun `auto icon style follows system dark mode when theme is SYSTEM`() {
        assertEquals(
            ResolvedAppIcon.NEON,
            AppIconManager.resolveIcon(ThemeModeSetting.SYSTEM, AppIconStyle.AUTO, isSystemDark = true),
        )
        assertEquals(
            ResolvedAppIcon.LIGHT,
            AppIconManager.resolveIcon(ThemeModeSetting.SYSTEM, AppIconStyle.AUTO, isSystemDark = false),
        )
    }

    @Test
    fun `manual icon style overrides theme mode`() {
        ThemeModeSetting.entries.forEach { theme ->
            assertEquals(
                ResolvedAppIcon.GLASS,
                AppIconManager.resolveIcon(theme, AppIconStyle.GLASS, isSystemDark = true),
            )
            assertEquals(
                ResolvedAppIcon.AERO,
                AppIconManager.resolveIcon(theme, AppIconStyle.AERO, isSystemDark = true),
            )
            assertEquals(
                ResolvedAppIcon.TERMINAL,
                AppIconManager.resolveIcon(theme, AppIconStyle.TERMINAL, isSystemDark = true),
            )
            assertEquals(
                ResolvedAppIcon.CLASSIC,
                AppIconManager.resolveIcon(theme, AppIconStyle.CLASSIC, isSystemDark = false),
            )
        }
    }

    @Test
    fun `exactly one launcher alias is enabled by default in manifest`() {
        val defaultEnabled = ResolvedAppIcon.entries.filter { it.defaultEnabledInManifest }
        assertEquals(listOf(ResolvedAppIcon.DARK), defaultEnabled)
    }
}
