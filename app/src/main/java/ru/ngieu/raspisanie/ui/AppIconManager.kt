package ru.ngieu.raspisanie.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Process
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import ru.ngieu.raspisanie.data.AppIconStyle
import ru.ngieu.raspisanie.data.ResolvedAppIcon
import ru.ngieu.raspisanie.data.ThemeModeSetting

data class SystemIconDiagnostics(
    val aliases: List<String> = emptyList(),
    val launcherAppsIcon: Bitmap? = null,
    val pmActivityIcon: Bitmap? = null,
    val pmAppIcon: Bitmap? = null,
)

data class IconApplyResult(
    val changed: Boolean,
    val activeIcon: ResolvedAppIcon?,
    val launcherAliases: List<String> = emptyList(),
    val errorMessage: String? = null,
)

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

    fun readSystemDiagnostics(context: Context): SystemIconDiagnostics {
        return runCatching {
            val pm = context.packageManager
            val pkg = context.packageName
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            val activities = launcherApps
                ?.getActivityList(pkg, Process.myUserHandle())
                .orEmpty()

            val aliases = if (activities.isNotEmpty()) {
                activities.map { it.componentName.shortClassName.substringAfterLast('.') }
            } else {
                readLauncherAppsAliases(context)
            }

            val launcherIconBitmap = runCatching {
                activities.firstOrNull()?.getIcon(0)?.toBitmap(width = 96, height = 96)
            }.getOrNull()

            val activeComponent = activities.firstOrNull()?.componentName ?: run {
                val active = readActiveIcon(context)
                active?.let { ComponentName(pkg, it.aliasClassName) }
            }

            val pmActivityBitmap = runCatching {
                activeComponent?.let { pm.getActivityIcon(it).toBitmap(width = 96, height = 96) }
            }.getOrNull()

            val pmAppBitmap = runCatching {
                pm.getApplicationIcon(pkg).toBitmap(width = 96, height = 96)
            }.getOrNull()

            SystemIconDiagnostics(
                aliases = aliases,
                launcherAppsIcon = launcherIconBitmap,
                pmActivityIcon = pmActivityBitmap,
                pmAppIcon = pmAppBitmap,
            )
        }.getOrDefault(SystemIconDiagnostics(aliases = readLauncherAppsAliases(context)))
    }

    fun readLauncherAppsAliases(context: Context): List<String> {
        return runCatching {
            val launcherApps = context.getSystemService(LauncherApps::class.java)
            val fromLauncherApps = launcherApps
                ?.getActivityList(context.packageName, Process.myUserHandle())
                ?.map { it.componentName.shortClassName.substringAfterLast('.') }
                .orEmpty()
            if (fromLauncherApps.isNotEmpty()) {
                return@runCatching fromLauncherApps
            }

            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(context.packageName)
            }
            context.packageManager
                .queryIntentActivities(launcherIntent, 0)
                .map { it.activityInfo.name.substringAfterLast('.') }
        }.getOrDefault(emptyList())
    }

    fun readActiveIcon(context: Context): ResolvedAppIcon? {
        return runCatching {
            val pm = context.packageManager
            val pkg = context.packageName

            val enabledBySetting = ResolvedAppIcon.entries.firstOrNull { icon ->
                isEffectivelyEnabled(pm, pkg, icon)
            }
            if (enabledBySetting != null) return@runCatching enabledBySetting

            val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(pkg)
            }
            val resolved = pm.queryIntentActivities(launcherIntent, 0)
            val resolvedName = resolved.firstOrNull()?.activityInfo?.name
            ResolvedAppIcon.entries.firstOrNull { it.aliasClassName == resolvedName }
        }.getOrNull()
    }

    fun syncIcon(
        context: Context,
        themeMode: ThemeModeSetting,
        iconStyle: AppIconStyle,
        isSystemDark: Boolean = isSystemDark(context),
        killApp: Boolean = false,
    ): IconApplyResult {
        val target = resolveIcon(
            themeMode = themeMode,
            iconStyle = iconStyle,
            isSystemDark = isSystemDark,
        )
        return applyResolvedIcon(
            context = context.applicationContext,
            target = target,
            killApp = killApp,
        )
    }

    fun applyResolvedIcon(
        context: Context,
        target: ResolvedAppIcon,
        killApp: Boolean = false,
    ): IconApplyResult {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName

            val targetAlreadyEnabled = isEffectivelyEnabled(pm, pkg, target)
            val othersToDisable = ResolvedAppIcon.entries.filter { icon ->
                icon != target && isEffectivelyEnabled(pm, pkg, icon)
            }

            if (targetAlreadyEnabled && othersToDisable.isEmpty()) {
                return IconApplyResult(
                    changed = false,
                    activeIcon = target,
                    launcherAliases = readLauncherAppsAliases(context),
                )
            }

            // Enable target alias first so the app never has zero enabled launcher entries.
            if (!targetAlreadyEnabled) {
                val enableFlags = if (killApp && othersToDisable.isEmpty()) {
                    0
                } else {
                    PackageManager.DONT_KILL_APP
                }
                pm.setComponentEnabledSetting(
                    ComponentName(pkg, target.aliasClassName),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    enableFlags,
                )
            }

            othersToDisable.forEachIndexed { index, icon ->
                val isLast = index == othersToDisable.lastIndex
                val disableFlags = if (killApp && isLast) 0 else PackageManager.DONT_KILL_APP
                pm.setComponentEnabledSetting(
                    ComponentName(pkg, icon.aliasClassName),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    disableFlags,
                )
            }

            IconApplyResult(
                changed = true,
                activeIcon = readActiveIcon(context) ?: target,
                launcherAliases = readLauncherAppsAliases(context),
            )
        } catch (t: Throwable) {
            IconApplyResult(
                changed = false,
                activeIcon = readActiveIcon(context),
                launcherAliases = readLauncherAppsAliases(context),
                errorMessage = t.message ?: t::class.java.simpleName,
            )
        }
    }

    suspend fun forceRefreshIcon(
        context: Context,
        target: ResolvedAppIcon,
    ): IconApplyResult {
        return try {
            val appCtx = context.applicationContext
            val pm = appCtx.packageManager
            val pkg = appCtx.packageName
            val tempAlias = ResolvedAppIcon.entries.last { it != target }

            // Phase 1: move to a temporary alias so PackageManager & LauncherApps observe a real transition.
            pm.setComponentEnabledSetting(
                ComponentName(pkg, tempAlias.aliasClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
            ResolvedAppIcon.entries.forEach { icon ->
                if (icon != tempAlias && isEffectivelyEnabled(pm, pkg, icon)) {
                    pm.setComponentEnabledSetting(
                        ComponentName(pkg, icon.aliasClassName),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP,
                    )
                }
            }

            delay(350L)

            // Phase 2: switch back to the target alias and flush immediately without DONT_KILL_APP.
            pm.setComponentEnabledSetting(
                ComponentName(pkg, target.aliasClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
            pm.setComponentEnabledSetting(
                ComponentName(pkg, tempAlias.aliasClassName),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                0,
            )

            IconApplyResult(
                changed = true,
                activeIcon = readActiveIcon(appCtx) ?: target,
                launcherAliases = readLauncherAppsAliases(appCtx),
            )
        } catch (t: Throwable) {
            IconApplyResult(
                changed = false,
                activeIcon = readActiveIcon(context),
                launcherAliases = readLauncherAppsAliases(context),
                errorMessage = t.message ?: t::class.java.simpleName,
            )
        }
    }

    private fun isEffectivelyEnabled(
        pm: PackageManager,
        pkg: String,
        icon: ResolvedAppIcon,
    ): Boolean {
        val component = ComponentName(pkg, icon.aliasClassName)
        return when (pm.getComponentEnabledSetting(component)) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            else -> icon.defaultEnabledInManifest
        }
    }
}
