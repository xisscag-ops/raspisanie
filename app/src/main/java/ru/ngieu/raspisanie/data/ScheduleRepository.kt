package ru.ngieu.raspisanie.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate

class ScheduleRepository(
    private val api: NgieuApiClient,
    private val cache: ScheduleCache,
) {
    suspend fun loadCached(): ScheduleSnapshot? = withContext(Dispatchers.IO) {
        val bundle = cache.loadBundle() ?: return@withContext null
        val lessons = withContext(Dispatchers.Default) {
            runCatching { ScheduleParser.parseSchedule(bundle.rawScheduleJson) }.getOrNull()
        } ?: return@withContext null
        val anchorDate = bundle.weekAnchorDateIso?.let(ScheduleParser::parseDate)
        ScheduleSnapshot(
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
            val cached = withContext(Dispatchers.IO) { cache.loadBundle() }
            val initialActorId = cached?.actorId ?: FALLBACK_ACTOR_ID

            coroutineScope {
                val actorsDeferred = async {
                    runCatching {
                        val actorsRaw = api.fetchStudentGroupsRaw()
                        withContext(Dispatchers.Default) {
                            val actors = ScheduleParser.parseActors(actorsRaw)
                            ScheduleParser.findGroupActorId(actors, TARGET_GROUP_NAME)
                        }
                    }.getOrNull()
                }
                val initialScheduleDeferred = async {
                    runCatching { api.fetchScheduleRaw(initialActorId) }
                }
                val weekTypeDeferred = async {
                    runCatching {
                        val raw = api.fetchWeekTypeRaw(today)
                        withContext(Dispatchers.Default) { ScheduleParser.parseWeekType(raw) }
                    }.getOrNull()
                }

                val resolvedActorId = actorsDeferred.await() ?: initialActorId
                val scheduleRaw = if (resolvedActorId != initialActorId) {
                    api.fetchScheduleRaw(resolvedActorId)
                } else {
                    initialScheduleDeferred.await().getOrThrow()
                }

                val lessons = withContext(Dispatchers.Default) {
                    ScheduleParser.parseSchedule(scheduleRaw)
                }
                val weekInfo = weekTypeDeferred.await()

                val anchorDate = weekInfo?.date
                    ?: cached?.weekAnchorDateIso?.let(ScheduleParser::parseDate)
                val anchorIsUpper = weekInfo?.isUpperWeek ?: cached?.weekAnchorIsUpper

                val nowMillis = System.currentTimeMillis()
                withContext(Dispatchers.IO) {
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
                }

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
