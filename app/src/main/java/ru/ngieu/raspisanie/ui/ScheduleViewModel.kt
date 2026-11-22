package ru.ngieu.raspisanie.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.ngieu.raspisanie.data.ActorKind
import ru.ngieu.raspisanie.data.AppUpdateInfo
import ru.ngieu.raspisanie.data.AppUpdater
import ru.ngieu.raspisanie.data.InstallLaunchResult
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.NgieuApiClient
import ru.ngieu.raspisanie.data.ScheduleActor
import ru.ngieu.raspisanie.data.ScheduleCache
import ru.ngieu.raspisanie.data.ScheduleParser
import ru.ngieu.raspisanie.data.ScheduleRepository
import ru.ngieu.raspisanie.data.ScheduleSnapshot
import ru.ngieu.raspisanie.data.SubgroupFilterMode
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.domain.WeekFilterMode
import ru.ngieu.raspisanie.ui.share.ScheduleShareBuilder
import ru.ngieu.raspisanie.ui.share.ScheduleShareRenderer
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

enum class ScheduleViewMode(val label: String) {
    DAY("Главная"),
    WEEK("Расписание"),
}

enum class MainNavTab(val label: String) {
    HOME("Главная"),
    WEEK("Неделя"),
    GROUP("Подгруппы"),
    SETTINGS("Настройки"),
}

sealed interface UpdateCheckState {
    data object Idle : UpdateCheckState
    data object Checking : UpdateCheckState
    data object UpToDate : UpdateCheckState
    data class Available(val info: AppUpdateInfo) : UpdateCheckState
    data class Downloading(val info: AppUpdateInfo, val progress: Float) : UpdateCheckState
    data class ReadyToInstall(val info: AppUpdateInfo, val apkPath: String) : UpdateCheckState
    data class NeedsInstallPermission(val info: AppUpdateInfo, val apkPath: String) : UpdateCheckState
    data class Error(val message: String) : UpdateCheckState
}

@Immutable
data class DayProgressSummary(
    val activeCount: Int = 0,
    val totalCount: Int = 0,
    val fraction: Float = 0f,
)

@Immutable
data class ScheduleUiState(
    val groupName: String = ScheduleRepository.TARGET_GROUP_NAME,
    val activeActorId: String = ScheduleRepository.FALLBACK_ACTOR_ID,
    val activeActorKind: ActorKind = ActorKind.GROUP,
    val availableActors: List<ScheduleActor> = emptyList(),
    val actorSearchFilter: ActorKind? = null,
    val isActorsLoading: Boolean = false,
    val anchorMonday: LocalDate = ScheduleEngine.mondayOf(initialDisplayDate()),
    val selectedDate: LocalDate = initialDisplayDate(),
    val todayDate: LocalDate = currentKnyagininoDate(),
    val manualWeekOverride: WeekFilterMode? = null,
    val viewMode: ScheduleViewMode = ScheduleViewMode.DAY,
    val activeTab: MainNavTab = MainNavTab.HOME,
    val searchQuery: String = "",
    val allLessons: List<Lesson> = emptyList(),
    val weekAnchorDate: LocalDate? = null,
    val weekAnchorIsUpper: Boolean? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOfflineFallback: Boolean = false,
    val errorMessage: String? = null,
    val updatedAtEpochMillis: Long? = null,
    val themeMode: ThemeModeSetting = ThemeModeSetting.NEON,
    val subgroupFilter: SubgroupFilterMode = SubgroupFilterMode.ALL,
    val nowTime: LocalTime = currentKnyagininoTime(),
    val weekDays: List<LocalDate> = emptyList(),
    val lessonsByWeekDay: Map<LocalDate, List<Lesson>> = emptyMap(),
    val liveLessonState: LiveLessonState = LiveLessonState.NoLessonsToday,
    val selectedDayProgress: DayProgressSummary = DayProgressSummary(),
    val updateState: UpdateCheckState = UpdateCheckState.Idle,
    val isSharingSchedule: Boolean = false,
) {
    val isViewingDefaultGroup: Boolean
        get() = activeActorKind == ActorKind.GROUP &&
            ScheduleParser.normalizeGroupName(groupName) ==
            ScheduleParser.normalizeGroupName(ScheduleRepository.TARGET_GROUP_NAME)

    val effectiveSubgroupFilter: SubgroupFilterMode
        get() = if (activeActorKind == ActorKind.GROUP) subgroupFilter else SubgroupFilterMode.ALL

    val selectedMonday: LocalDate
        get() = ScheduleEngine.mondayOf(selectedDate)

    val detectedWeekForSelectedMonday: WeekFilterMode?
        get() = detectedWeekForMonday(selectedMonday)

    val detectedCurrentWeek: WeekFilterMode?
        get() = detectedWeekForMonday(ScheduleEngine.mondayOf(todayDate))

    val activeWeekFilter: WeekFilterMode
        get() = weekFilterForMonday(selectedMonday)

    fun detectedWeekForMonday(monday: LocalDate): WeekFilterMode? {
        return ScheduleEngine.isWeekUpperForMonday(
            anchorDate = weekAnchorDate,
            anchorIsUpper = weekAnchorIsUpper,
            targetMonday = monday,
        )?.let { if (it) WeekFilterMode.UPPER else WeekFilterMode.LOWER }
    }

    fun weekFilterForMonday(monday: LocalDate): WeekFilterMode {
        return if (monday == selectedMonday && manualWeekOverride != null) {
            manualWeekOverride
        } else {
            detectedWeekForMonday(monday) ?: WeekFilterMode.UPPER
        }
    }

    fun matchesSearch(lesson: Lesson): Boolean {
        val q = searchQuery.trim()
        if (q.isEmpty()) return true
        return lesson.subject.contains(q, ignoreCase = true) ||
            lesson.room.contains(q, ignoreCase = true) ||
            lesson.teachers.any { it.contains(q, ignoreCase = true) } ||
            lesson.groups.any { it.contains(q, ignoreCase = true) } ||
            lesson.note.contains(q, ignoreCase = true)
    }
}

private val KNYAGININO_ZONE: ZoneId = ZoneId.of("Europe/Moscow")

private fun currentKnyagininoDate(): LocalDate = LocalDate.now(KNYAGININO_ZONE)

private fun currentKnyagininoTime(): LocalTime = LocalTime.now(KNYAGININO_ZONE)

private fun initialDisplayDate(): LocalDate {
    val today = currentKnyagininoDate()
    return if (today.dayOfWeek == DayOfWeek.SUNDAY) today.plusDays(1) else today
}

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(
        api = NgieuApiClient(),
        cache = ScheduleCache(application),
    )
    private val appUpdater = AppUpdater()

    private val _uiState = MutableStateFlow(
        recomputeDerived(
            ScheduleUiState(
                themeMode = repository.loadThemeMode(),
                subgroupFilter = repository.loadSubgroupFilter(),
            ),
        ),
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        startClockTicker()
        checkForUpdates(silent = true)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val cachedActors = repository.loadCachedActors()
            if (cachedActors.isNotEmpty()) {
                _uiState.update { it.copy(availableActors = cachedActors) }
            }
            val cached = repository.loadCached()
            if (cached != null) {
                updateState { applySnapshot(it, cached, isOfflineFallback = false).copy(isLoading = false) }
            }
            refresh(silent = cached != null)
        }
    }

    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value
            updateState { state ->
                state.copy(
                    isLoading = state.allLessons.isEmpty() && !silent,
                    isRefreshing = true,
                    errorMessage = null,
                    todayDate = currentKnyagininoDate(),
                    nowTime = currentKnyagininoTime(),
                )
            }

            val result = if (currentState.isViewingDefaultGroup) {
                repository.refreshFromNetwork(currentKnyagininoDate())
            } else {
                repository.loadScheduleForActor(
                    actor = ScheduleActor(
                        id = currentState.activeActorId,
                        name = currentState.groupName,
                        kind = currentState.activeActorKind,
                    ),
                    today = currentKnyagininoDate(),
                    forceRefresh = true,
                )
            }
            val latestActors = repository.loadCachedActors()

            result.fold(
                onSuccess = { snapshot ->
                    updateState { state ->
                        applySnapshot(state, snapshot, isOfflineFallback = false).copy(
                            availableActors = if (latestActors.isNotEmpty()) latestActors else state.availableActors,
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    updateState { state ->
                        val hasCached = state.allLessons.isNotEmpty()
                        state.copy(
                            availableActors = if (latestActors.isNotEmpty()) latestActors else state.availableActors,
                            isLoading = false,
                            isRefreshing = false,
                            isOfflineFallback = hasCached,
                            errorMessage = if (hasCached) {
                                null
                            } else {
                                error.message ?: "Не удалось загрузить расписание"
                            },
                        )
                    }
                },
            )
        }
    }

    private fun applySnapshot(
        state: ScheduleUiState,
        snapshot: ScheduleSnapshot,
        isOfflineFallback: Boolean,
    ): ScheduleUiState {
        return state.copy(
            groupName = snapshot.groupName,
            activeActorId = snapshot.actorId,
            activeActorKind = snapshot.actorKind,
            allLessons = snapshot.lessons,
            weekAnchorDate = snapshot.weekAnchorDate,
            weekAnchorIsUpper = snapshot.weekAnchorIsUpper,
            updatedAtEpochMillis = snapshot.updatedAtEpochMillis,
            isOfflineFallback = isOfflineFallback,
        )
    }

    fun selectDate(date: LocalDate) {
        updateState { state ->
            val mondayChanged = ScheduleEngine.mondayOf(date) != state.selectedMonday
            state.copy(
                selectedDate = date,
                manualWeekOverride = if (mondayChanged) null else state.manualWeekOverride,
            )
        }
    }

    fun shiftWeek(deltaWeeks: Long) {
        updateState { state ->
            val shiftedDate = state.selectedDate.plusWeeks(deltaWeeks)
            state.copy(
                selectedDate = shiftedDate,
                manualWeekOverride = null,
            )
        }
    }

    fun goToToday() {
        val target = initialDisplayDate()
        updateState {
            it.copy(
                todayDate = currentKnyagininoDate(),
                selectedDate = target,
                manualWeekOverride = null,
            )
        }
    }

    fun setWeekOverride(mode: WeekFilterMode) {
        updateState { state ->
            val detected = state.detectedWeekForSelectedMonday
            val newOverride = if (detected == mode) null else mode
            state.copy(manualWeekOverride = newOverride)
        }
    }

    fun selectTab(tab: MainNavTab) {
        updateState { state ->
            val nextViewMode = when (tab) {
                MainNavTab.HOME -> ScheduleViewMode.DAY
                MainNavTab.WEEK -> ScheduleViewMode.WEEK
                else -> state.viewMode
            }
            state.copy(
                activeTab = tab,
                viewMode = nextViewMode,
            )
        }
    }

    fun setViewMode(mode: ScheduleViewMode) {
        updateState {
            it.copy(
                viewMode = mode,
                activeTab = if (mode == ScheduleViewMode.DAY) MainNavTab.HOME else MainNavTab.WEEK,
            )
        }
    }

    fun setSearchQuery(query: String) {
        updateState { it.copy(searchQuery = query) }
        if (query.isNotBlank() && _uiState.value.availableActors.isEmpty()) {
            ensureActorsLoaded()
        }
    }

    fun setActorSearchFilter(filter: ActorKind?) {
        _uiState.update { it.copy(actorSearchFilter = filter) }
        if (_uiState.value.availableActors.isEmpty()) {
            ensureActorsLoaded()
        }
    }

    fun ensureActorsLoaded() {
        val current = _uiState.value
        if (current.isActorsLoading || current.availableActors.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isActorsLoading = true) }
            val result = repository.refreshActorsFromNetwork()
            result.fold(
                onSuccess = { actors ->
                    _uiState.update { it.copy(availableActors = actors, isActorsLoading = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isActorsLoading = false) }
                },
            )
        }
    }

    fun selectActor(actor: ScheduleActor) {
        val memoryHit = repository.getMemorySnapshot(actor.id)
        if (memoryHit != null) {
            updateState { state ->
                applySnapshot(state, memoryHit, isOfflineFallback = false).copy(
                    searchQuery = "",
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            updateState { state ->
                state.copy(
                    groupName = actor.name,
                    activeActorId = actor.id,
                    activeActorKind = actor.kind,
                    searchQuery = "",
                    isRefreshing = true,
                    errorMessage = null,
                )
            }
            val result = repository.loadScheduleForActor(
                actor = actor,
                today = currentKnyagininoDate(),
                forceRefresh = false,
            )
            result.fold(
                onSuccess = { snapshot ->
                    updateState { state ->
                        applySnapshot(state, snapshot, isOfflineFallback = false).copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    updateState { state ->
                        state.copy(
                            isLoading = false,
                            isRefreshing = false,
                        )
                    }
                    Toast.makeText(
                        getApplication(),
                        error.message ?: "Не удалось загрузить расписание для ${actor.name}",
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }

    fun resetToDefaultGroup() {
        val defaultActor = _uiState.value.availableActors.firstOrNull {
            it.kind == ActorKind.GROUP &&
                ScheduleParser.normalizeGroupName(it.name) ==
                ScheduleParser.normalizeGroupName(ScheduleRepository.TARGET_GROUP_NAME)
        } ?: ScheduleActor(
            id = ScheduleRepository.FALLBACK_ACTOR_ID,
            name = ScheduleRepository.TARGET_GROUP_NAME,
            kind = ActorKind.GROUP,
        )
        selectActor(defaultActor)
    }

    fun setThemeMode(mode: ThemeModeSetting) {
        repository.saveThemeMode(mode)
        updateState { it.copy(themeMode = mode) }
    }

    fun setSubgroupFilter(mode: SubgroupFilterMode) {
        repository.saveSubgroupFilter(mode)
        updateState { it.copy(subgroupFilter = mode) }
    }

    fun checkForUpdates(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                updateState { it.copy(updateState = UpdateCheckState.Checking) }
            }
            val result = appUpdater.checkForUpdate()
            result.fold(
                onSuccess = { info ->
                    updateState { state ->
                        if (info != null) {
                            state.copy(updateState = UpdateCheckState.Available(info))
                        } else if (!silent) {
                            state.copy(updateState = UpdateCheckState.UpToDate)
                        } else {
                            state
                        }
                    }
                },
                onFailure = { error ->
                    if (!silent) {
                        updateState {
                            it.copy(
                                updateState = UpdateCheckState.Error(
                                    error.message ?: "Не удалось проверить обновления",
                                ),
                            )
                        }
                    }
                },
            )
        }
    }

    fun downloadAndInstallUpdate() {
        val info = when (val current = _uiState.value.updateState) {
            is UpdateCheckState.Available -> current.info
            is UpdateCheckState.ReadyToInstall -> {
                launchInstallerForFile(current.info, File(current.apkPath))
                return
            }
            is UpdateCheckState.NeedsInstallPermission -> {
                launchInstallerForFile(current.info, File(current.apkPath))
                return
            }
            else -> return
        }

        viewModelScope.launch {
            updateState { it.copy(updateState = UpdateCheckState.Downloading(info, 0f)) }
            val downloadResult = appUpdater.downloadApk(
                context = getApplication(),
                info = info,
                onProgress = { progress ->
                    _uiState.update { current ->
                        current.copy(updateState = UpdateCheckState.Downloading(info, progress))
                    }
                },
            )
            downloadResult.fold(
                onSuccess = { apkFile ->
                    launchInstallerForFile(info, apkFile)
                },
                onFailure = { error ->
                    updateState {
                        it.copy(
                            updateState = UpdateCheckState.Error(
                                error.message ?: "Не удалось скачать обновление",
                            ),
                        )
                    }
                },
            )
        }
    }

    private fun launchInstallerForFile(info: AppUpdateInfo, apkFile: File) {
        if (!apkFile.exists()) {
            updateState { it.copy(updateState = UpdateCheckState.Available(info)) }
            return
        }
        runCatching {
            appUpdater.launchInstaller(getApplication(), apkFile)
        }.fold(
            onSuccess = { result ->
                updateState {
                    it.copy(
                        updateState = when (result) {
                            InstallLaunchResult.LAUNCHED ->
                                UpdateCheckState.ReadyToInstall(info, apkFile.absolutePath)
                            InstallLaunchResult.PERMISSION_REQUIRED ->
                                UpdateCheckState.NeedsInstallPermission(info, apkFile.absolutePath)
                        },
                    )
                }
            },
            onFailure = { error ->
                updateState {
                    it.copy(
                        updateState = UpdateCheckState.Error(
                            error.message ?: "Не удалось запустить установку APK",
                        ),
                    )
                }
            },
        )
    }

    fun shareDaySchedule(context: Context) {
        val snapshotState = _uiState.value
        if (snapshotState.isSharingSchedule || snapshotState.allLessons.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSharingSchedule = true) }
            try {
                val shareSnapshot = ScheduleShareBuilder.buildDaySnapshot(
                    actorName = snapshotState.groupName,
                    actorKind = snapshotState.activeActorKind,
                    selectedDate = snapshotState.selectedDate,
                    weekFilter = snapshotState.activeWeekFilter,
                    allLessons = snapshotState.allLessons,
                    subgroupFilter = snapshotState.effectiveSubgroupFilter,
                )
                ScheduleShareRenderer.renderAndShareDay(
                    context = context,
                    snapshot = shareSnapshot,
                    todayDate = snapshotState.todayDate,
                    themeMode = snapshotState.themeMode,
                    updatedAtEpochMillis = snapshotState.updatedAtEpochMillis,
                )
            } catch (e: Throwable) {
                Toast.makeText(
                    context,
                    "Не удалось подготовить скриншот: ${e.message ?: "ошибка"}",
                    Toast.LENGTH_SHORT,
                ).show()
            } finally {
                _uiState.update { it.copy(isSharingSchedule = false) }
            }
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(15_000L)
                updateState {
                    it.copy(
                        todayDate = currentKnyagininoDate(),
                        nowTime = currentKnyagininoTime(),
                    )
                }
            }
        }
    }

    private inline fun updateState(transform: (ScheduleUiState) -> ScheduleUiState) {
        _uiState.update { current ->
            recomputeDerived(transform(current))
        }
    }

    private fun recomputeDerived(state: ScheduleUiState): ScheduleUiState {
        val monday = state.selectedMonday
        val weekDays = (0L..5L).map { monday.plusDays(it) }
        val activeFilter = state.activeWeekFilter
        val activeSubgroup = state.effectiveSubgroupFilter
        val lessonsByDay = weekDays.associateWith { date ->
            ScheduleEngine.lessonsForDay(
                allLessons = state.allLessons,
                targetDate = date,
                weekFilter = activeFilter,
                subgroupFilter = activeSubgroup,
            ).filter(state::matchesSearch)
        }

        val todayLessons = ScheduleEngine.lessonsForDay(
            allLessons = state.allLessons,
            targetDate = state.todayDate,
            weekFilter = state.detectedCurrentWeek ?: activeFilter,
            subgroupFilter = activeSubgroup,
        )
        val liveState = ScheduleEngine.computeLiveLessonState(todayLessons, state.nowTime)

        val selectedLessons = lessonsByDay[state.selectedDate].orEmpty()
        val selectedSlots = ScheduleEngine.groupLessonsIntoSlots(selectedLessons)
        val total = selectedSlots.size
        val activeCount = when {
            total == 0 -> 0
            state.selectedDate.isBefore(state.todayDate) -> total
            state.selectedDate.isAfter(state.todayDate) -> 0
            else -> selectedSlots.count { slot ->
                slot.startTime != null && !state.nowTime.isBefore(slot.startTime)
            }
        }
        val fraction = if (total == 0) 0f else (activeCount.toFloat() / total.toFloat()).coerceIn(0f, 1f)

        return state.copy(
            weekDays = weekDays,
            lessonsByWeekDay = lessonsByDay,
            liveLessonState = liveState,
            selectedDayProgress = DayProgressSummary(
                activeCount = activeCount,
                totalCount = total,
                fraction = fraction,
            ),
        )
    }
}
