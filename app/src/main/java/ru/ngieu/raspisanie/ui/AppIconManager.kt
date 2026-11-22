package ru.ngieu.raspisanie.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import ru.ngieu.raspisanie.data.AppIconStyle
import ru.ngieu.raspisanie.data.ResolvedAppIcon
import ru.ngieu.raspisanie.data.ThemeModeSetting

object AppIconManager {

    fun resolveIcon(
        themeMode: ThemeModeSetting,
        iconStyle: AppIconStyle,
        isSystemDark: Boolean,
    ): ResolvedAppIcon {
        return when (iconStyle) {
            AppIconStyle.AUTO -> when (themeMode) {
                ThemeModeSetting.NEON -> ResolvedAppIcon.NEON
                ThemeModeSetting.GLASS -> ResolvedAppIcon.GLASS
                ThemeModeSetting.LIGHT -> ResolvedAppIcon.LIGHT
                ThemeModeSetting.DARK -> ResolvedAppIcon.DARK
                ThemeModeSetting.SYSTEM -> if (isSystemDark) ResolvedAppIcon.NEON else ResolvedAppIcon.LIGHT
            }
            AppIconStyle.NEON -> ResolvedAppIcon.NEON
            AppIconStyle.GLASS -> ResolvedAppIcon.GLASS
            AppIconStyle.LIGHT -> ResolvedAppIcon.LIGHT
            AppIconStyle.DARK -> ResolvedAppIcon.DARK
            AppIconStyle.CLASSIC -> ResolvedAppIcon.CLASSIC
        }
    }

    fun isSystemDark(context: Context): Boolean {
        val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightMask == Configuration.UI_MODE_NIGHT_YES
    }

    fun syncIcon(
        context: Context,
        themeMode: ThemeModeSetting,
        iconStyle: AppIconStyle,
        isSystemDark: Boolean = isSystemDark(context),
    ): Boolean {
        val target = resolveIcon(
            themeMode = themeMode,
            iconStyle = iconStyle,
            isSystemDark = isSystemDark,
        )
        return applyResolvedIcon(context.applicationContext, target)
    }

    fun applyResolvedIcon(
        context: Context,
        target: ResolvedAppIcon,
    ): Boolean {
        return runCatching {
            val pm = context.packageManager
            val pkg = context.packageName

            fun isEffectivelyEnabled(icon: ResolvedAppIcon): Boolean {
                val component = ComponentName(pkg, icon.aliasClassName)
                return when (pm.getComponentEnabledSetting(component)) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
                    else -> icon.defaultEnabledInManifest
                }
            }

            val alreadySynced = isEffectivelyEnabled(target) &&
                ResolvedAppIcon.entries.all { it == target || !isEffectivelyEnabled(it) }
            if (alreadySynced) return@runCatching false

            // Enable the target launcher alias first so the app never has zero enabled launcher entries.
            if (!isEffectivelyEnabled(target)) {
                pm.setComponentEnabledSetting(
                    ComponentName(pkg, target.aliasClassName),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }

            ResolvedAppIcon.entries.forEach { icon ->
                if (icon != target && isEffectivelyEnabled(icon)) {
                    pm.setComponentEnabledSetting(
                        ComponentName(pkg, icon.aliasClassName),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP,
                    )
                }
            }
            true
        }.getOrDefault(false)
    }
}
