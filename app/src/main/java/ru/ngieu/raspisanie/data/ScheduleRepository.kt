package ru.ngieu.raspisanie.data

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class ScheduleRepository(
    private val api: NgieuApiClient,
    private val cache: ScheduleCache,
) {
    fun loadCached(): ScheduleSnapshot? {
        val bundle = cache.loadBundle() ?: return null
        val lessons = runCatching { ScheduleParser.parseSchedule(bundle.rawScheduleJson) }
            .getOrNull()
            ?: return null
        val anchorDate = bundle.weekAnchorDateIso?.let(ScheduleParser::parseDate)
        return ScheduleSnapshot(
            groupName = bundle.groupName,
            actorId = bundle.actorId,
            lessons = lessons,
            weekAnchorDate = anchorDate,
            weekAnchorIsUpper = bundle.weekAnchorIsUpper,
            updatedAtEpochMillis = bundle.updatedAtEpochMillis,
            isFromCache = true,
        )
    }

    suspend fun refreshFromNetwork(today: LocalDate): Result<ScheduleSnapshot> {
        return runCatching {
            val cached = cache.loadBundle()

            val resolvedActorId = runCatching {
                val actorsRaw = api.fetchStudentGroupsRaw()
                val actors = ScheduleParser.parseActors(actorsRaw)
                ScheduleParser.findGroupActorId(actors, TARGET_GROUP_NAME)
            }.getOrNull()
                ?: cached?.actorId
                ?: FALLBACK_ACTOR_ID

            coroutineScope {
                val scheduleDeferred = async { api.fetchScheduleRaw(resolvedActorId) }
                val weekTypeDeferred = async {
                    runCatching {
                        val raw = api.fetchWeekTypeRaw(today)
                        ScheduleParser.parseWeekType(raw)
                    }.getOrNull()
                }

                val scheduleRaw = scheduleDeferred.await()
                val lessons = ScheduleParser.parseSchedule(scheduleRaw)
                val weekInfo = weekTypeDeferred.await()

                val anchorDate = weekInfo?.date
                    ?: cached?.weekAnchorDateIso?.let(ScheduleParser::parseDate)
                val anchorIsUpper = weekInfo?.isUpperWeek ?: cached?.weekAnchorIsUpper

                val nowMillis = System.currentTimeMillis()
                cache.saveBundle(
                    CachedScheduleBundle(
                        actorId = resolvedActorId,
                        groupName = TARGET_GROUP_NAME,
                        rawScheduleJson = scheduleRaw,
                        weekAnchorDateIso = anchorDate?.toString(),
                        weekAnchorIsUpper = anchorIsUpper,
                        updatedAtEpochMillis = nowMillis,
                    )
                )

                ScheduleSnapshot(
                    groupName = TARGET_GROUP_NAME,
                    actorId = resolvedActorId,
                    lessons = lessons,
                    weekAnchorDate = anchorDate,
                    weekAnchorIsUpper = anchorIsUpper,
                    updatedAtEpochMillis = nowMillis,
                    isFromCache = false,
                )
            }
        }
    }

    fun loadThemeMode(): ThemeModeSetting = cache.loadThemeMode()

    fun saveThemeMode(mode: ThemeModeSetting) = cache.saveThemeMode(mode)

    companion object {
        const val TARGET_GROUP_NAME = "С-25ИО"
        const val FALLBACK_ACTOR_ID = "a11ce001-0000-0001-0000-00a200000000"
    }
}
