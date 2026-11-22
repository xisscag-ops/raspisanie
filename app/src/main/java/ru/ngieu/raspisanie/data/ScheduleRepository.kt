package ru.ngieu.raspisanie.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

class ScheduleRepository(
    private val api: NgieuApiClient,
    private val cache: ScheduleCache,
) {
    private val memorySchedules = ConcurrentHashMap<String, ScheduleSnapshot>()

    @Volatile
    private var cachedWeekAnchorDate: LocalDate? = null

    @Volatile
    private var cachedWeekAnchorIsUpper: Boolean? = null

    suspend fun loadCached(): ScheduleSnapshot? = withContext(Dispatchers.IO) {
        val bundle = cache.loadBundle() ?: return@withContext null
        val lessons = withContext(Dispatchers.Default) {
            runCatching { ScheduleParser.parseSchedule(bundle.rawScheduleJson) }.getOrNull()
        } ?: return@withContext null
        val anchorDate = bundle.weekAnchorDateIso?.let(ScheduleParser::parseDate)
        cachedWeekAnchorDate = anchorDate
        cachedWeekAnchorIsUpper = bundle.weekAnchorIsUpper
        val snapshot = ScheduleSnapshot(
            groupName = bundle.groupName,
            actorId = bundle.actorId,
            lessons = lessons,
            weekAnchorDate = anchorDate,
            weekAnchorIsUpper = bundle.weekAnchorIsUpper,
            updatedAtEpochMillis = bundle.updatedAtEpochMillis,
            isFromCache = true,
            actorKind = ActorKind.GROUP,
        )
        memorySchedules[bundle.actorId] = snapshot
        snapshot
    }

    suspend fun loadCachedActors(): List<ScheduleActor> = withContext(Dispatchers.IO) {
        cache.loadActors()?.actors.orEmpty()
    }

    suspend fun refreshActorsFromNetwork(): Result<List<ScheduleActor>> = runCatching {
        coroutineScope {
            val groupsDeferred = async {
                runCatching {
                    val raw = api.fetchStudentGroupsRaw()
                    withContext(Dispatchers.Default) {
                        ScheduleParser.parseTypedActors(raw, ActorKind.GROUP)
                    }
                }.getOrDefault(emptyList())
            }
            val teachersDeferred = async {
                runCatching {
                    val raw = api.fetchTeachersRaw()
                    withContext(Dispatchers.Default) {
                        ScheduleParser.parseTypedActors(raw, ActorKind.TEACHER)
                    }
                }.getOrDefault(emptyList())
            }

            val groups = groupsDeferred.await()
            val teachers = teachersDeferred.await()
            val combined = (groups + teachers).distinctBy { it.id }
            if (combined.isEmpty()) {
                throw IllegalStateException("Не удалось загрузить список групп и преподавателей")
            }
            withContext(Dispatchers.IO) {
                cache.saveActors(
                    CachedActorsBundle(
                        actors = combined,
                        updatedAtEpochMillis = System.currentTimeMillis(),
                    ),
                )
            }
            combined
        }
    }

    suspend fun refreshFromNetwork(today: LocalDate): Result<ScheduleSnapshot> {
        return runCatching {
            val cached = withContext(Dispatchers.IO) { cache.loadBundle() }
            val initialActorId = cached?.actorId ?: FALLBACK_ACTOR_ID

            coroutineScope {
                val actorsDeferred = async {
                    runCatching {
                        val groupsRaw = api.fetchStudentGroupsRaw()
                        val teachersRaw = runCatching { api.fetchTeachersRaw() }.getOrNull()
                        withContext(Dispatchers.Default) {
                            val groups = ScheduleParser.parseTypedActors(groupsRaw, ActorKind.GROUP)
                            val teachers = teachersRaw?.let {
                                ScheduleParser.parseTypedActors(it, ActorKind.TEACHER)
                            }.orEmpty()
                            (groups + teachers).distinctBy { it.id }
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

                val actors = actorsDeferred.await()
                if (!actors.isNullOrEmpty()) {
                    withContext(Dispatchers.IO) {
                        cache.saveActors(
                            CachedActorsBundle(
                                actors = actors,
                                updatedAtEpochMillis = System.currentTimeMillis(),
                            ),
                        )
                    }
                }

                val resolvedActorId = actors
                    ?.firstOrNull {
                        it.kind == ActorKind.GROUP &&
                            ScheduleParser.normalizeGroupName(it.name) ==
                            ScheduleParser.normalizeGroupName(TARGET_GROUP_NAME)
                    }?.id
                    ?: initialActorId

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
                    ?: cachedWeekAnchorDate
                val anchorIsUpper = weekInfo?.isUpperWeek
                    ?: cached?.weekAnchorIsUpper
                    ?: cachedWeekAnchorIsUpper

                cachedWeekAnchorDate = anchorDate
                cachedWeekAnchorIsUpper = anchorIsUpper

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

                val snapshot = ScheduleSnapshot(
                    groupName = TARGET_GROUP_NAME,
                    actorId = resolvedActorId,
                    lessons = lessons,
                    weekAnchorDate = anchorDate,
                    weekAnchorIsUpper = anchorIsUpper,
                    updatedAtEpochMillis = nowMillis,
                    isFromCache = false,
                    actorKind = ActorKind.GROUP,
                )
                memorySchedules[resolvedActorId] = snapshot
                snapshot
            }
        }
    }

    suspend fun loadScheduleForActor(
        actor: ScheduleActor,
        today: LocalDate,
        forceRefresh: Boolean = false,
    ): Result<ScheduleSnapshot> = runCatching {
        if (!forceRefresh) {
            memorySchedules[actor.id]?.let { return@runCatching it }
            if (actor.kind == ActorKind.GROUP &&
                ScheduleParser.normalizeGroupName(actor.name) ==
                ScheduleParser.normalizeGroupName(TARGET_GROUP_NAME)
            ) {
                loadCached()?.let { return@runCatching it }
            }
        }

        coroutineScope {
            val scheduleDeferred = async { api.fetchScheduleRaw(actor.id) }
            val weekTypeDeferred = if (cachedWeekAnchorDate == null || forceRefresh) {
                async {
                    runCatching {
                        val raw = api.fetchWeekTypeRaw(today)
                        withContext(Dispatchers.Default) { ScheduleParser.parseWeekType(raw) }
                    }.getOrNull()
                }
            } else {
                null
            }

            val scheduleRaw = scheduleDeferred.await()
            val lessons = withContext(Dispatchers.Default) {
                ScheduleParser.parseSchedule(scheduleRaw)
            }
            val weekInfo = weekTypeDeferred?.await()
            val anchorDate = weekInfo?.date ?: cachedWeekAnchorDate
            val anchorIsUpper = weekInfo?.isUpperWeek ?: cachedWeekAnchorIsUpper
            cachedWeekAnchorDate = anchorDate
            cachedWeekAnchorIsUpper = anchorIsUpper

            val nowMillis = System.currentTimeMillis()
            val isDefaultGroup = actor.kind == ActorKind.GROUP &&
                ScheduleParser.normalizeGroupName(actor.name) ==
                ScheduleParser.normalizeGroupName(TARGET_GROUP_NAME)

            if (isDefaultGroup) {
                withContext(Dispatchers.IO) {
                    cache.saveBundle(
                        CachedScheduleBundle(
                            actorId = actor.id,
                            groupName = TARGET_GROUP_NAME,
                            rawScheduleJson = scheduleRaw,
                            weekAnchorDateIso = anchorDate?.toString(),
                            weekAnchorIsUpper = anchorIsUpper,
                            updatedAtEpochMillis = nowMillis,
                        ),
                    )
                }
            }

            val snapshot = ScheduleSnapshot(
                groupName = actor.name,
                actorId = actor.id,
                lessons = lessons,
                weekAnchorDate = anchorDate,
                weekAnchorIsUpper = anchorIsUpper,
                updatedAtEpochMillis = nowMillis,
                isFromCache = false,
                actorKind = actor.kind,
            )
            memorySchedules[actor.id] = snapshot
            snapshot
        }
    }

    fun getMemorySnapshot(actorId: String): ScheduleSnapshot? = memorySchedules[actorId]

    fun loadThemeMode(): ThemeModeSetting = cache.loadThemeMode()

    fun saveThemeMode(mode: ThemeModeSetting) = cache.saveThemeMode(mode)

    fun loadSubgroupFilter(): SubgroupFilterMode = cache.loadSubgroupFilter()

    fun saveSubgroupFilter(mode: SubgroupFilterMode) = cache.saveSubgroupFilter(mode)

    fun loadTetrisBestScore(): Int = cache.loadTetrisBestScore()

    fun saveTetrisBestScore(score: Int) = cache.saveTetrisBestScore(score)

    companion object {
        const val TARGET_GROUP_NAME = "С-25ИО"
        const val FALLBACK_ACTOR_ID = "a11ce001-0000-0001-0000-00a200000000"
    }
}
