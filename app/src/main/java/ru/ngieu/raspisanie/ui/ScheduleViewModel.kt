package ru.ngieu.raspisanie.ui

import android.app.Application
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

data class ScheduleUiState(
    val groupName: String = ScheduleRepository.TARGET_GROUP_NAME,
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
) {
    val selectedMonday: LocalDate
        get() = ScheduleEngine.mondayOf(selectedDate)

    val detectedWeekForSelectedMonday: WeekFilterMode?
        get() = ScheduleEngine.isWeekUpperForMonday(
            anchorDate = weekAnchorDate,
            anchorIsUpper = weekAnchorIsUpper,
            targetMonday = selectedMonday,
        )?.let { if (it) WeekFilterMode.UPPER else WeekFilterMode.LOWER }

    val detectedCurrentWeek: WeekFilterMode?
        get() = ScheduleEngine.isWeekUpperForMonday(
            anchorDate = weekAnchorDate,
            anchorIsUpper = weekAnchorIsUpper,
            targetMonday = ScheduleEngine.mondayOf(todayDate),
        )?.let { if (it) WeekFilterMode.UPPER else WeekFilterMode.LOWER }

    val activeWeekFilter: WeekFilterMode
        get() = manualWeekOverride ?: detectedWeekForSelectedMonday ?: WeekFilterMode.UPPER

    val lessonsForSelectedDay: List<Lesson>
        get() = ScheduleEngine.lessonsForDay(
            allLessons = allLessons,
            targetDate = selectedDate,
            weekFilter = activeWeekFilter,
        )

    val weekDays: List<LocalDate>
        get() = (0L..5L).map { selectedMonday.plusDays(it) }

    val lessonsByWeekDay: Map<LocalDate, List<Lesson>>
        get() = weekDays.associateWith { date ->
            ScheduleEngine.lessonsForDay(
                allLessons = allLessons,
                targetDate = date,
                weekFilter = activeWeekFilter,
            )
        }

    val liveLessonState: LiveLessonState
        get() {
            val todayLessons = ScheduleEngine.lessonsForDay(
                allLessons = allLessons,
                targetDate = todayDate,
                weekFilter = detectedCurrentWeek ?: activeWeekFilter,
            )
            return ScheduleEngine.computeLiveLessonState(todayLessons, nowTime)
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
        ScheduleUiState(themeMode = repository.loadThemeMode())
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        startClockTicker()
    }

    private fun loadInitialData() {
        val cached = repository.loadCached()
        if (cached != null) {
            applySnapshot(cached, isOfflineFallback = false)
            _uiState.update { it.copy(isLoading = false) }
        }
        refresh(silent = cached != null)
    }

    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { state ->
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
                    applySnapshot(snapshot, isOfflineFallback = false)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
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

    private fun applySnapshot(snapshot: ScheduleSnapshot, isOfflineFallback: Boolean) {
        _uiState.update { state ->
            state.copy(
                groupName = snapshot.groupName,
                allLessons = snapshot.lessons,
                weekAnchorDate = snapshot.weekAnchorDate,
                weekAnchorIsUpper = snapshot.weekAnchorIsUpper,
                updatedAtEpochMillis = snapshot.updatedAtEpochMillis,
                isOfflineFallback = isOfflineFallback,
            )
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun shiftWeek(deltaWeeks: Long) {
        _uiState.update { state ->
            val shiftedDate = state.selectedDate.plusWeeks(deltaWeeks)
            state.copy(
                selectedDate = shiftedDate,
                manualWeekOverride = null,
            )
        }
    }

    fun goToToday() {
        val target = initialDisplayDate()
        _uiState.update {
            it.copy(
                todayDate = currentKnyagininoDate(),
                selectedDate = target,
                manualWeekOverride = null,
            )
        }
    }

    fun setWeekOverride(mode: WeekFilterMode) {
        _uiState.update { state ->
            val detected = state.detectedWeekForSelectedMonday
            val newOverride = if (detected == mode) null else mode
            state.copy(manualWeekOverride = newOverride)
        }
    }

    fun setViewMode(mode: ScheduleViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun setThemeMode(mode: ThemeModeSetting) {
        repository.saveThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    private fun startClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(30_000L)
                _uiState.update {
                    it.copy(
                        todayDate = currentKnyagininoDate(),
                        nowTime = currentKnyagininoTime(),
                    )
                }
            }
        }
    }
}
