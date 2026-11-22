package ru.ngieu.raspisanie.ui

import android.app.Application
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
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.NgieuApiClient
import ru.ngieu.raspisanie.data.ScheduleCache
import ru.ngieu.raspisanie.data.ScheduleRepository
import ru.ngieu.raspisanie.data.ScheduleSnapshot
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.domain.WeekFilterMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

enum class ScheduleViewMode(val label: String) {
    DAY("День"),
    WEEK("Вся неделя"),
}

@Immutable
data class ScheduleUiState(
    val groupName: String = ScheduleRepository.TARGET_GROUP_NAME,
    val anchorMonday: LocalDate = ScheduleEngine.mondayOf(initialDisplayDate()),
    val selectedDate: LocalDate = initialDisplayDate(),
    val todayDate: LocalDate = currentKnyagininoDate(),
    val manualWeekOverride: WeekFilterMode? = null,
    val viewMode: ScheduleViewMode = ScheduleViewMode.DAY,
    val allLessons: List<Lesson> = emptyList(),
    val weekAnchorDate: LocalDate? = null,
    val weekAnchorIsUpper: Boolean? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isOfflineFallback: Boolean = false,
    val errorMessage: String? = null,
    val updatedAtEpochMillis: Long? = null,
    val themeMode: ThemeModeSetting = ThemeModeSetting.SYSTEM,
    val nowTime: LocalTime = currentKnyagininoTime(),
    val weekDays: List<LocalDate> = emptyList(),
    val lessonsByWeekDay: Map<LocalDate, List<Lesson>> = emptyMap(),
    val liveLessonState: LiveLessonState = LiveLessonState.NoLessonsToday,
) {
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

    private val _uiState = MutableStateFlow(
        recomputeDerived(ScheduleUiState(themeMode = repository.loadThemeMode()))
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        startClockTicker()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val cached = repository.loadCached()
            if (cached != null) {
                updateState { applySnapshot(it, cached, isOfflineFallback = false).copy(isLoading = false) }
            }
            refresh(silent = cached != null)
        }
    }

    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            updateState { state ->
                state.copy(
                    isLoading = state.allLessons.isEmpty() && !silent,
                    isRefreshing = true,
                    errorMessage = null,
                    todayDate = currentKnyagininoDate(),
                    nowTime = currentKnyagininoTime(),
                )
            }

            val result = repository.refreshFromNetwork(currentKnyagininoDate())
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
                        val hasCached = state.allLessons.isNotEmpty()
                        state.copy(
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

    fun setViewMode(mode: ScheduleViewMode) {
        updateState { it.copy(viewMode = mode) }
    }

    fun setThemeMode(mode: ThemeModeSetting) {
        repository.saveThemeMode(mode)
        updateState { it.copy(themeMode = mode) }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(30_000L)
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
        val lessonsByDay = weekDays.associateWith { date ->
            ScheduleEngine.lessonsForDay(
                allLessons = state.allLessons,
                targetDate = date,
                weekFilter = activeFilter,
            )
        }

        val todayLessons = ScheduleEngine.lessonsForDay(
            allLessons = state.allLessons,
            targetDate = state.todayDate,
            weekFilter = state.detectedCurrentWeek ?: activeFilter,
        )
        val liveState = ScheduleEngine.computeLiveLessonState(todayLessons, state.nowTime)

        return state.copy(
            weekDays = weekDays,
            lessonsByWeekDay = lessonsByDay,
            liveLessonState = liveState,
        )
    }
}
