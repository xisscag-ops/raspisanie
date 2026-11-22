package ru.ngieu.raspisanie.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.material.icons.rounded.ViewWeek
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.domain.WeekFilterMode
import ru.ngieu.raspisanie.ui.components.BellsAndSettingsBottomSheet
import ru.ngieu.raspisanie.ui.components.DaySelectorRow
import ru.ngieu.raspisanie.ui.components.LessonCard
import ru.ngieu.raspisanie.ui.components.LiveLessonBanner
import ru.ngieu.raspisanie.ui.components.WeekSelectorBar
import ru.ngieu.raspisanie.ui.components.fullDayName
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ruLocale = Locale("ru", "RU")
private val dayHeaderDateFormatter = DateTimeFormatter.ofPattern("d MMMM", ruLocale)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onSelectDate: (java.time.LocalDate) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onGoToday: () -> Unit,
    onSelectWeekMode: (WeekFilterMode) -> Unit,
    onToggleViewMode: () -> Unit,
    onSelectTheme: (ru.ngieu.raspisanie.data.ThemeModeSetting) -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }

    val currentLessonId = (state.liveLessonState as? LiveLessonState.InProgress)?.currentLesson?.id

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Группа ${state.groupName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        val weekLabel = state.detectedCurrentWeek?.let {
                            "НГИЭУ • Сейчас ${it.title.lowercase()} неделя"
                        } ?: "НГИЭУ • Расписание занятий"
                        Text(
                            text = weekLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleViewMode) {
                        Icon(
                            imageVector = if (state.viewMode == ScheduleViewMode.DAY) {
                                Icons.Rounded.ViewWeek
                            } else {
                                Icons.Rounded.ViewDay
                            },
                            contentDescription = if (state.viewMode == ScheduleViewMode.DAY) {
                                "Показать всю неделю"
                            } else {
                                "Показать по дням"
                            },
                        )
                    }
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = "Звонки и настройки",
                        )
                    }
                    IconButton(
                        onClick = onRefresh,
                        enabled = !state.isRefreshing,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Обновить расписание",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Controls header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                if (state.isOfflineFallback) {
                    OfflineNoticeBanner(
                        updatedAtEpochMillis = state.updatedAtEpochMillis,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                WeekSelectorBar(
                    selectedMonday = state.selectedMonday,
                    activeWeekFilter = state.activeWeekFilter,
                    detectedWeekForSelectedMonday = state.detectedWeekForSelectedMonday,
                    showTodayButton = state.selectedDate != state.todayDate || state.manualWeekOverride != null,
                    onPreviousWeek = { onShiftWeek(-1) },
                    onNextWeek = { onShiftWeek(1) },
                    onGoToday = onGoToday,
                    onSelectWeekMode = onSelectWeekMode,
                )

                if (state.viewMode == ScheduleViewMode.DAY) {
                    Spacer(modifier = Modifier.height(10.dp))
                    DaySelectorRow(
                        weekDays = state.weekDays,
                        selectedDate = state.selectedDate,
                        todayDate = state.todayDate,
                        lessonsByDay = state.lessonsByWeekDay,
                        onSelectDate = onSelectDate,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main content area with Pull-to-Refresh
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Загружаем расписание ${state.groupName}...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    state.errorMessage != null && state.allLessons.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.error,
                                )
                                Text(
                                    text = "Не удалось загрузить расписание",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = state.errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                                Button(onClick = onRefresh) {
                                    Text("Повторить")
                                }
                            }
                        }
                    }

                    state.viewMode == ScheduleViewMode.DAY -> {
                        val dayLessons = state.lessonsForSelectedDay
                        val isViewingToday = state.selectedDate == state.todayDate &&
                            (state.manualWeekOverride == null || state.manualWeekOverride == state.detectedCurrentWeek)

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 24.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (isViewingToday) {
                                item(key = "live_banner") {
                                    LiveLessonBanner(state = state.liveLessonState)
                                }
                            }

                            item(key = "day_title") {
                                val title = "${fullDayName(state.selectedDate.dayOfWeek)}, ${state.selectedDate.format(dayHeaderDateFormatter)}"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, bottom = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = pairsCountLabel(dayLessons.size),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            if (dayLessons.isEmpty()) {
                                item(key = "empty_day") {
                                    EmptyDayCard()
                                }
                            } else {
                                items(
                                    items = dayLessons,
                                    key = { it.id },
                                ) { lesson ->
                                    LessonCard(
                                        lesson = lesson,
                                        isCurrentNow = isViewingToday && currentLessonId == lesson.id,
                                        themeMode = state.themeMode,
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        // Full Week View
                        val lessonsByDay = state.lessonsByWeekDay
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 4.dp,
                                bottom = 24.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            if (state.selectedMonday == ru.ngieu.raspisanie.domain.ScheduleEngine.mondayOf(state.todayDate)) {
                                item(key = "week_live_banner") {
                                    LiveLessonBanner(state = state.liveLessonState)
                                }
                            }

                            state.weekDays.forEach { date ->
                                val dayLessons = lessonsByDay[date].orEmpty()
                                val isToday = date == state.todayDate

                                item(key = "header_$date") {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                text = "${fullDayName(date.dayOfWeek)}, ${date.format(dayHeaderDateFormatter)}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isToday) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onBackground
                                                },
                                            )
                                            if (isToday) {
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                ) {
                                                    Text(
                                                        text = "Сегодня",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = pairsCountLabel(dayLessons.size),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }

                                if (dayLessons.isEmpty()) {
                                    item(key = "empty_$date") {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        ) {
                                            Text(
                                                text = "Занятий нет",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            )
                                        }
                                    }
                                } else {
                                    items(
                                        items = dayLessons,
                                        key = { "${date}_${it.id}" },
                                    ) { lesson ->
                                        LessonCard(
                                            lesson = lesson,
                                            isCurrentNow = isToday && currentLessonId == lesson.id,
                                            themeMode = state.themeMode,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        BellsAndSettingsBottomSheet(
            groupName = state.groupName,
            themeMode = state.themeMode,
            updatedAtEpochMillis = state.updatedAtEpochMillis,
            onSelectTheme = onSelectTheme,
            onDismiss = { showBottomSheet = false },
        )
    }
}

@Composable
private fun OfflineNoticeBanner(
    updatedAtEpochMillis: Long?,
    modifier: Modifier = Modifier,
) {
    val timeLabel = updatedAtEpochMillis?.let { millis ->
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.of("Europe/Moscow"))
            .format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = if (timeLabel != null) {
                    "Оффлайн-режим • данные от $timeLabel"
                } else {
                    "Оффлайн-режим • показан сохранённый кэш"
                },
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun EmptyDayCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 36.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.EventAvailable,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "В этот день пар нет",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Можно отдохнуть или проверить другую неделю",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun pairsCountLabel(count: Int): String {
    return when {
        count == 0 -> "Нет пар"
        count % 10 == 1 && count % 100 != 11 -> "$count пара"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "$count пары"
        else -> "$count пар"
    }
}
