package ru.ngieu.raspisanie.data

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

class ScheduleCache(context: Context) {

    private val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
    private val actorsCacheFile = File(context.filesDir, ACTORS_CACHE_FILE_NAME)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveBundle(bundle: CachedScheduleBundle) {
        runCatching {
            val encoded = ScheduleParser.json.encodeToString(bundle)
            cacheFile.writeText(encoded, Charsets.UTF_8)
        }
    }

    fun loadBundle(): CachedScheduleBundle? {
        return runCatching {
            if (!cacheFile.exists()) return null
            val raw = cacheFile.readText(Charsets.UTF_8)
            if (raw.isBlank()) return null
            ScheduleParser.json.decodeFromString<CachedScheduleBundle>(raw)
        }.getOrNull()
    }

    fun saveActors(bundle: CachedActorsBundle) {
        runCatching {
            val encoded = ScheduleParser.json.encodeToString(bundle)
            actorsCacheFile.writeText(encoded, Charsets.UTF_8)
        }
    }

    fun loadActors(): CachedActorsBundle? {
        return runCatching {
            if (!actorsCacheFile.exists()) return null
            val raw = actorsCacheFile.readText(Charsets.UTF_8)
            if (raw.isBlank()) return null
            ScheduleParser.json.decodeFromString<CachedActorsBundle>(raw)
        }.getOrNull()
    }

    fun saveThemeMode(mode: ThemeModeSetting) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun loadThemeMode(): ThemeModeSetting {
        val raw = prefs.getString(KEY_THEME_MODE, ThemeModeSetting.NEON.name)
        return runCatching { ThemeModeSetting.valueOf(raw ?: ThemeModeSetting.NEON.name) }
            .getOrDefault(ThemeModeSetting.NEON)
    }

    private companion object {
        const val CACHE_FILE_NAME = "schedule_cache_c25io.json"
        const val ACTORS_CACHE_FILE_NAME = "actors_cache_v1.json"
        const val PREFS_NAME = "ngieu_raspisanie_prefs"
        const val KEY_THEME_MODE = "theme_mode_v2"
    }
}
