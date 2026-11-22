package ru.ngieu.raspisanie.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.ngieu.raspisanie.BuildConfig
import ru.ngieu.raspisanie.R
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.data.WeekParity
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.domain.WeekFilterMode
import ru.ngieu.raspisanie.ui.components.BellsAndSettingsBottomSheet
import ru.ngieu.raspisanie.ui.components.DaySelectorBottomDock
import ru.ngieu.raspisanie.ui.components.GlassCard
import ru.ngieu.raspisanie.ui.components.LessonCard
import ru.ngieu.raspisanie.ui.components.LiveLessonBanner
import ru.ngieu.raspisanie.ui.components.NeonAuroraBackground
import ru.ngieu.raspisanie.ui.components.NeonTopNavBar
import ru.ngieu.raspisanie.ui.components.WeekSelectorBar
import ru.ngieu.raspisanie.ui.components.fullDayName
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ruLocale = Locale("ru", "RU")
private val dayHeaderDateFormatter = DateTimeFormatter.ofPattern("d MMMM", ruLocale)
private val shortWeekRangeFormatter = DateTimeFormatter.ofPattern("d MMM", ruLocale)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    onRefresh: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onGoToday: () -> Unit,
    onSelectWeekMode: (WeekFilterMode) -> Unit,
    onSelectTab: (MainNavTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectTheme: (ThemeModeSetting) -> Unit,
) {
    val style = LocalAppVisualStyle.current
    var showWeekPickerSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var inspectedLesson by remember { mutableStateOf<Lesson?>(null) }

    val currentLessonId = (state.liveLessonState as? LiveLessonState.InProgress)?.currentLesson?.id

    NeonAuroraBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                // Верхний заголовок в стиле Neon Glass
                NeonTopHeader(
                    groupName = state.groupName,
                    weekSubtitle = "${state.activeWeekFilter.title} неделя",
                    showTodayPill = state.selectedDate != state.todayDate || state.manualWeekOverride != null,
                    isSearchVisible = showSearchBar,
                    onClickWeekPicker = { showWeekPickerSheet = true },
                    onClickToday = onGoToday,
                    onToggleSearch = {
                        showSearchBar = !showSearchBar
                        if (!showSearchBar && state.searchQuery.isNotEmpty()) {
                            onSearchQueryChange("")
                        }
                    },
                    onOpenSettings = { showSettingsSheet = true },
                )

                // Верхняя панель навигации (Главная, Неделя, Звонки, Настройки)
                NeonTopNavBar(
                    activeTab = state.activeTab,
                    onSelectTab = onSelectTab,
                )

                // Раскрывающийся поиск
                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = expandVertically(tween(110)) + fadeIn(tween(110)),
                    exit = shrinkVertically(tween(90)) + fadeOut(tween(90)),
                ) {
                    NeonSearchBar(
                        query = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                // Оффлайн-уведомление
                AnimatedVisibility(
                    visible = state.isOfflineFallback,
                    enter = expandVertically(tween(110)) + fadeIn(tween(110)),
                    exit = shrinkVertically(tween(90)) + fadeOut(tween(90)),
                ) {
                    OfflineNoticeBanner(
                        updatedAtEpochMillis = state.updatedAtEpochMillis,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    )
                }

                // Основной контент вкладок
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.weight(1f),
                ) {
                    AnimatedContent(
                        targetState = state.activeTab,
                        transitionSpec = { fadeIn(tween(100)) togetherWith fadeOut(tween(80)) },
                        label = "tabTransition",
                    ) { tab ->
                        when (tab) {
                            MainNavTab.HOME -> {
                                HomeScheduleTab(
                                    state = state,
                                    currentLessonId = currentLessonId,
                                    onSelectDate = onSelectDate,
                                    onInspectLesson = { inspectedLesson = it },
                                    onRefresh = onRefresh,
                                )
                            }

                            MainNavTab.WEEK -> {
                                WeekScheduleTab(
                                    state = state,
                                    currentLessonId = currentLessonId,
                                    onSelectDate = onSelectDate,
                                    onShiftWeek = onShiftWeek,
                                    onSelectWeekMode = onSelectWeekMode,
                                    onInspectLesson = { inspectedLesson = it },
                                    onRefresh = onRefresh,
                                )
                            }

                            MainNavTab.GROUP -> {
                                GroupAndBellsTab(
                                    state = state,
                                    onRefresh = onRefresh,
                                )
                            }

                            MainNavTab.SETTINGS -> {
                                SettingsTab(
                                    state = state,
                                    onSelectTheme = onSelectTheme,
                                    onSelectWeekMode = onSelectWeekMode,
                                    onGoToday = onGoToday,
                                    onRefresh = onRefresh,
                                )
                            }
                        }
                    }
                }
            }

            // Нижняя панель выбора дней недели (под большим пальцем)
            AnimatedVisibility(
                visible = state.activeTab == MainNavTab.HOME,
                enter = fadeIn(tween(90)) + slideInVertically(tween(110)) { it / 2 },
                exit = fadeOut(tween(80)) + slideOutVertically(tween(90)) { it / 2 },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                DaySelectorBottomDock(
                    weekDays = state.weekDays,
                    selectedDate = state.selectedDate,
                    todayDate = state.todayDate,
                    lessonsByDay = state.lessonsByWeekDay,
                    onSelectDate = onSelectDate,
                )
            }
        }
    }

    if (showWeekPickerSheet) {
        ModalBottomSheet(onDismissRequest = { showWeekPickerSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
            ) {
                Text(
                    text = "Выбор недели и чётности",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(12.dp))
                WeekSelectorBar(
                    selectedMonday = state.selectedMonday,
                    activeWeekFilter = state.activeWeekFilter,
                    detectedWeekForSelectedMonday = state.detectedWeekForSelectedMonday,
                    showTodayButton = state.selectedDate != state.todayDate || state.manualWeekOverride != null,
                    onPreviousWeek = { onShiftWeek(-1) },
                    onNextWeek = { onShiftWeek(1) },
                    onGoToday = {
                        onGoToday()
                        showWeekPickerSheet = false
                    },
                    onSelectWeekMode = onSelectWeekMode,
                )
            }
        }
    }

    if (showSettingsSheet) {
        BellsAndSettingsBottomSheet(
            groupName = state.groupName,
            themeMode = state.themeMode,
            updatedAtEpochMillis = state.updatedAtEpochMillis,
            onSelectTheme = onSelectTheme,
            onDismiss = { showSettingsSheet = false },
        )
    }

    inspectedLesson?.let { lesson ->
        LessonDetailsBottomSheet(
            lesson = lesson,
            isDark = style.isDark,
            onDismiss = { inspectedLesson = null },
        )
    }
}

@Composable
private fun NeonTopHeader(
    groupName: String,
    weekSubtitle: String,
    showTodayPill: Boolean,
    isSearchVisible: Boolean,
    onClickWeekPicker: () -> Unit,
    onClickToday: () -> Unit,
    onToggleSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val titleColor = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onBackground
    val subtitleColor = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Группа $groupName",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onClickWeekPicker)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "НГИЭУ • $weekSubtitle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = subtitleColor,
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_neon_expand),
                        contentDescription = "Выбрать неделю",
                        colorFilter = if (style.isNeon) null else ColorFilter.tint(subtitleColor),
                        modifier = Modifier.size(13.dp),
                    )
                }

                AnimatedVisibility(visible = showTodayPill) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (style.isNeon) Color(0x409333EA) else MaterialTheme.colorScheme.primaryContainer,
                            )
                            .clickable(onClick = onClickToday)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "Сегодня",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (style.isNeon) Color(0xFFE9D5FF) else MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderCircleButton(
                iconRes = R.drawable.ic_neon_search,
                contentDescription = "Поиск по расписанию",
                highlighted = isSearchVisible,
                onClick = onToggleSearch,
            )
            HeaderCircleButton(
                iconRes = R.drawable.ic_neon_settings,
                contentDescription = "Звонки и настройки",
                highlighted = false,
                onClick = onOpenSettings,
            )
        }
    }
}

@Composable
private fun HeaderCircleButton(
    iconRes: Int,
    contentDescription: String,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val fill = when {
        highlighted && style.isNeon -> Brush.verticalGradient(listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd))
        highlighted -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primaryContainer))
        style.isNeon -> Brush.verticalGradient(listOf(Color(0xBA242D58), Color(0xB3161E41)))
        else -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }
    val border = if (style.isNeon) {
        Brush.verticalGradient(listOf(NeonPalette.glassBorderTop, NeonPalette.glassBorderBottom))
    } else {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant))
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(brush = fill, shape = CircleShape)
            .border(width = 1.dp, brush = border, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            colorFilter = if (style.isNeon) null else ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.size(21.dp),
        )
    }
}

@Composable
private fun NeonSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalAppVisualStyle.current
    val textColor = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSurface
    val hintColor = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_neon_search),
                contentDescription = null,
                colorFilter = if (style.isNeon) null else ColorFilter.tint(hintColor),
                modifier = Modifier.size(18.dp),
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Поиск по предмету, преподавателю или ауд.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = hintColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                    cursorBrush = SolidColor(if (style.isNeon) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(22.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Очистить поиск",
                        tint = hintColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScheduleTab(
    state: ScheduleUiState,
    currentLessonId: String?,
    onSelectDate: (LocalDate) -> Unit,
    onInspectLesson: (Lesson) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(4.dp))

        when {
            state.isLoading -> LoadingView(groupName = state.groupName)
            state.errorMessage != null && state.allLessons.isEmpty() -> ErrorView(
                errorMessage = state.errorMessage,
                onRefresh = onRefresh,
            )
            else -> DayHorizontalPager(
                state = state,
                currentLessonId = currentLessonId,
                onSelectDate = onSelectDate,
                onInspectLesson = onInspectLesson,
            )
        }
    }
}

@Composable
private fun DayHorizontalPager(
    state: ScheduleUiState,
    currentLessonId: String?,
    onSelectDate: (LocalDate) -> Unit,
    onInspectLesson: (Lesson) -> Unit,
) {
    val latestSelectedDate by rememberUpdatedState(state.selectedDate)
    val initialPage = remember {
        ScheduleEngine.dateToDayIndex(state.anchorMonday, state.selectedDate)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { ScheduleEngine.TOTAL_DAY_PAGES },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val pageDate = ScheduleEngine.dayIndexToDate(state.anchorMonday, settledPage)
                if (pageDate != latestSelectedDate) {
                    onSelectDate(pageDate)
                }
            }
    }

    LaunchedEffect(state.selectedDate) {
        val targetPage = ScheduleEngine.dateToDayIndex(state.anchorMonday, state.selectedDate)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        modifier = Modifier.fillMaxSize(),
    ) { pageIndex ->
        val pageDate = remember(state.anchorMonday, pageIndex) {
            ScheduleEngine.dayIndexToDate(state.anchorMonday, pageIndex)
        }
        val pageMonday = remember(pageDate) {
            ScheduleEngine.mondayOf(pageDate)
        }
        val pageWeekFilter = state.weekFilterForMonday(pageMonday)
        val daySlots = remember(
            state.allLessons,
            state.lessonsByWeekDay,
            state.searchQuery,
            pageDate,
            pageMonday,
            state.selectedMonday,
            pageWeekFilter,
        ) {
            val rawLessons = if (pageMonday == state.selectedMonday) {
                state.lessonsByWeekDay[pageDate].orEmpty()
            } else {
                ScheduleEngine.lessonsForDay(
                    allLessons = state.allLessons,
                    targetDate = pageDate,
                    weekFilter = pageWeekFilter,
                ).filter(state::matchesSearch)
            }
            ScheduleEngine.groupLessonsIntoSlots(rawLessons)
        }

        val isViewingToday = pageDate == state.todayDate &&
            (state.manualWeekOverride == null || state.manualWeekOverride == state.detectedCurrentWeek)

        val pageProgress = remember(daySlots, isViewingToday, pageDate, state.todayDate, state.nowTime) {
            if (pageDate == state.selectedDate) {
                state.selectedDayProgress
            } else {
                val total = daySlots.size
                val active = when {
                    total == 0 -> 0
                    pageDate.isBefore(state.todayDate) -> total
                    pageDate.isAfter(state.todayDate) -> 0
                    else -> daySlots.count { it.startTime != null && !state.nowTime.isBefore(it.startTime) }
                }
                DayProgressSummary(
                    activeCount = active,
                    totalCount = total,
                    fraction = if (total == 0) 0f else (active.toFloat() / total.toFloat()).coerceIn(0f, 1f),
                )
            }
        }

        DayLessonsList(
            pageDate = pageDate,
            daySlots = daySlots,
            isViewingToday = isViewingToday,
            liveLessonState = state.liveLessonState,
            progressSummary = pageProgress,
            currentLessonId = currentLessonId,
            nowTime = state.nowTime,
            themeMode = state.themeMode,
            hasSearchQuery = state.searchQuery.isNotBlank(),
            onInspectLesson = onInspectLesson,
        )
    }
}

@Composable
private fun DayLessonsList(
    pageDate: LocalDate,
    daySlots: List<LessonSlot>,
    isViewingToday: Boolean,
    liveLessonState: LiveLessonState,
    progressSummary: DayProgressSummary,
    currentLessonId: String?,
    nowTime: LocalTime,
    themeMode: ThemeModeSetting,
    hasSearchQuery: Boolean,
    onInspectLesson: (Lesson) -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = navBottomPadding + 104.dp,
        ),
    ) {
        if (progressSummary.totalCount > 0 || (isViewingToday && liveLessonState !is LiveLessonState.NoLessonsToday)) {
            item(key = "live_banner_$pageDate") {
                LiveLessonBanner(
                    state = liveLessonState,
                    progressSummary = progressSummary,
                    isViewingToday = isViewingToday,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
        }

        item(key = "day_title_$pageDate") {
            val title = "${fullDayName(pageDate.dayOfWeek)}, ${pageDate.format(dayHeaderDateFormatter)}"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = pairsCountLabel(daySlots.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (daySlots.isEmpty()) {
            item(key = "empty_day_$pageDate") {
                EmptyDayCard(hasSearchQuery = hasSearchQuery)
            }
        } else {
            itemsIndexed(
                items = daySlots,
                key = { _, slot -> slot.id },
            ) { index, slot ->
                val isSlotCurrentNow = isViewingToday && isSlotRunningNow(slot, currentLessonId, nowTime)
                LessonCard(
                    slot = slot,
                    isCurrentNow = isSlotCurrentNow,
                    themeMode = themeMode,
                    isFirst = index == 0,
                    isLast = index == daySlots.lastIndex,
                    onClickLesson = onInspectLesson,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun WeekScheduleTab(
    state: ScheduleUiState,
    currentLessonId: String?,
    onSelectDate: (LocalDate) -> Unit,
    onShiftWeek: (Long) -> Unit,
    onSelectWeekMode: (WeekFilterMode) -> Unit,
    onInspectLesson: (Lesson) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WeekSelectorBar(
            selectedMonday = state.selectedMonday,
            activeWeekFilter = state.activeWeekFilter,
            detectedWeekForSelectedMonday = state.detectedWeekForSelectedMonday,
            showTodayButton = false,
            onPreviousWeek = { onShiftWeek(-1) },
            onNextWeek = { onShiftWeek(1) },
            onGoToday = {},
            onSelectWeekMode = onSelectWeekMode,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        Spacer(modifier = Modifier.height(6.dp))

        when {
            state.isLoading -> LoadingView(groupName = state.groupName)
            state.errorMessage != null && state.allLessons.isEmpty() -> ErrorView(
                errorMessage = state.errorMessage,
                onRefresh = onRefresh,
            )
            else -> WeekHorizontalPager(
                state = state,
                currentLessonId = currentLessonId,
                onSelectDate = onSelectDate,
                onInspectLesson = onInspectLesson,
            )
        }
    }
}

@Composable
private fun WeekHorizontalPager(
    state: ScheduleUiState,
    currentLessonId: String?,
    onSelectDate: (LocalDate) -> Unit,
    onInspectLesson: (Lesson) -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val latestSelectedDate by rememberUpdatedState(state.selectedDate)
    val initialPage = remember {
        ScheduleEngine.mondayToWeekIndex(state.anchorMonday, state.selectedMonday)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { ScheduleEngine.TOTAL_WEEK_PAGES },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val pageMonday = ScheduleEngine.weekIndexToMonday(state.anchorMonday, settledPage)
                val currentMonday = ScheduleEngine.mondayOf(latestSelectedDate)
                if (pageMonday != currentMonday) {
                    val dayOffset = (latestSelectedDate.dayOfWeek.value - 1).coerceIn(0, 5).toLong()
                    onSelectDate(pageMonday.plusDays(dayOffset))
                }
            }
    }

    LaunchedEffect(state.selectedMonday) {
        val targetPage = ScheduleEngine.mondayToWeekIndex(state.anchorMonday, state.selectedMonday)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        modifier = Modifier.fillMaxSize(),
    ) { pageIndex ->
        val pageMonday = remember(state.anchorMonday, pageIndex) {
            ScheduleEngine.weekIndexToMonday(state.anchorMonday, pageIndex)
        }
        val pageWeekDays = remember(pageMonday) {
            (0L..5L).map { pageMonday.plusDays(it) }
        }
        val pageWeekFilter = state.weekFilterForMonday(pageMonday)
        val slotsByDay = remember(
            state.allLessons,
            state.lessonsByWeekDay,
            state.searchQuery,
            pageMonday,
            state.selectedMonday,
            pageWeekFilter,
        ) {
            val map = if (pageMonday == state.selectedMonday) {
                state.lessonsByWeekDay
            } else {
                pageWeekDays.associateWith { date ->
                    ScheduleEngine.lessonsForDay(
                        allLessons = state.allLessons,
                        targetDate = date,
                        weekFilter = pageWeekFilter,
                    ).filter(state::matchesSearch)
                }
            }
            map.mapValues { (_, list) -> ScheduleEngine.groupLessonsIntoSlots(list) }
        }

        val isCurrentWeek = pageMonday == ScheduleEngine.mondayOf(state.todayDate)
        val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = navBottomPadding + 24.dp,
            ),
        ) {
            if (isCurrentWeek && state.selectedDayProgress.totalCount > 0) {
                item(key = "week_live_banner") {
                    LiveLessonBanner(
                        state = state.liveLessonState,
                        progressSummary = state.selectedDayProgress,
                        isViewingToday = state.selectedDate == state.todayDate,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
            }

            pageWeekDays.forEach { date ->
                val daySlots = slotsByDay[date].orEmpty()
                val isToday = date == state.todayDate

                item(key = "header_$date") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp),
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
                                color = when {
                                    isToday && style.isNeon -> Color(0xFF38BDF8)
                                    isToday -> MaterialTheme.colorScheme.primary
                                    style.isNeon -> NeonPalette.textPrimary
                                    else -> MaterialTheme.colorScheme.onBackground
                                },
                            )
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (style.isNeon) Color(0x3838BDF8) else MaterialTheme.colorScheme.primaryContainer,
                                        )
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "Сегодня",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (style.isNeon) Color(0xFF7DD3FC) else MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                        Text(
                            text = pairsCountLabel(daySlots.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (daySlots.isEmpty()) {
                    item(key = "empty_$date") {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            cornerRadius = 14.dp,
                        ) {
                            Text(
                                text = "Занятий нет",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = daySlots,
                        key = { _, slot -> "${date}_${slot.id}" },
                    ) { index, slot ->
                        val isSlotCurrentNow = isToday && isSlotRunningNow(slot, currentLessonId, state.nowTime)
                        LessonCard(
                            slot = slot,
                            isCurrentNow = isSlotCurrentNow,
                            themeMode = state.themeMode,
                            isFirst = index == 0,
                            isLast = index == daySlots.lastIndex,
                            onClickLesson = onInspectLesson,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupAndBellsTab(
    state: ScheduleUiState,
    onRefresh: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val titleColor = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSurface
    val subColor = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    val updatedLabel = state.updatedAtEpochMillis?.let { millis ->
        val formatted = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.of("Europe/Moscow"))
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm"))
        "Синхронизация: $formatted (МСК)"
    } ?: "Ожидается синхронизация"
    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 6.dp,
            bottom = navBottomPadding + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                glowColor = if (style.isNeon) Color(0xFF7C3AED) else null,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_neon_group),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Группа ${state.groupName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                            )
                            Text(
                                text = "Княгининский университет (НГИЭУ)",
                                style = MaterialTheme.typography.bodySmall,
                                color = subColor,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = updatedLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = subColor,
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 14.dp,
                        onClick = if (!state.isRefreshing) onRefresh else null,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = if (style.isNeon) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isRefreshing) "Обновляем..." else "Обновить расписание",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor,
                            )
                        }
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Расписание звонков",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ScheduleEngine.standardBells.forEachIndexed { index, bell ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${bell.pairNumber} пара",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor,
                            )
                            Text(
                                text = "${bell.start} – ${bell.end}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subColor,
                            )
                        }
                        if (index < ScheduleEngine.standardBells.lastIndex) {
                            HorizontalDivider(
                                color = if (style.isNeon) Color(0x1EFFFFFF) else MaterialTheme.colorScheme.outlineVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    state: ScheduleUiState,
    onSelectTheme: (ThemeModeSetting) -> Unit,
    onSelectWeekMode: (WeekFilterMode) -> Unit,
    onGoToday: () -> Unit,
    onRefresh: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val titleColor = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSurface
    val subColor = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 6.dp,
            bottom = navBottomPadding + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_neon_themes),
                            contentDescription = null,
                            colorFilter = if (style.isNeon) null else ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Тема оформления",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeModeSetting.entries.forEach { mode ->
                            val selected = mode == state.themeMode
                            val pillBrush = if (selected && style.isNeon) {
                                Brush.verticalGradient(listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd))
                            } else if (selected) {
                                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                            } else {
                                null
                            }

                            GlassCard(
                                modifier = Modifier.weight(1f),
                                cornerRadius = 12.dp,
                                fillBrush = pillBrush,
                                onClick = { onSelectTheme(mode) },
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else subColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Неделя расписания",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WeekFilterMode.entries.forEach { mode ->
                            val selected = state.activeWeekFilter == mode
                            val pillBrush = if (selected && style.isNeon) {
                                Brush.verticalGradient(listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd))
                            } else if (selected) {
                                Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                            } else {
                                null
                            }
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                cornerRadius = 12.dp,
                                fillBrush = pillBrush,
                                onClick = { onSelectWeekMode(mode) },
                            ) {
                                Text(
                                    text = "${mode.title} неделя",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else subColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp,
                            onClick = onGoToday,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Today,
                                    contentDescription = null,
                                    tint = if (style.isNeon) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "На сегодня",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleColor,
                                )
                            }
                        }
                        GlassCard(
                            modifier = Modifier.weight(1f),
                            cornerRadius = 12.dp,
                            onClick = if (!state.isRefreshing) onRefresh else null,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = if (style.isNeon) Color(0xFFA855F7) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (state.isRefreshing) "Обновление..." else "Обновить",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = titleColor,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "О приложении",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Расписание НГИЭУ • Группа ${state.groupName}\nВерсия ${BuildConfig.VERSION_NAME} (сборка ${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        color = subColor,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LessonDetailsBottomSheet(
    lesson: Lesson,
    isDark: Boolean,
    onDismiss: () -> Unit,
) {
    val typeColor = ScheduleAccentColors.lessonTypeColor(lesson.lessonType, isDark)
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = typeColor.copy(alpha = 0.16f),
                    contentColor = typeColor,
                ) {
                    Text(
                        text = lesson.lessonType.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                if (lesson.pairNumber > 0) {
                    Text(
                        text = "${lesson.pairNumber} пара • ${lesson.timeRangeText}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = lesson.timeRangeText,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = lesson.subject,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            if (lesson.room.isNotBlank()) {
                Text(
                    text = "Аудитория: ${lesson.room}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (lesson.teachers.isNotEmpty()) {
                Text(
                    text = "Преподаватель: ${lesson.teachers.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (lesson.parity != WeekParity.EVERY) {
                Text(
                    text = "Периодичность: ${lesson.parity.label} неделя",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (lesson.note.isNotBlank()) {
                Text(
                    text = "Примечание: ${lesson.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (lesson.isChange) {
                Text(
                    text = "Внимание: это замена в расписании",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun LoadingView(groupName: String) {
    val style = LocalAppVisualStyle.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CircularProgressIndicator(
                color = if (style.isNeon) Color(0xFFA855F7) else MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Загружаем расписание $groupName...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    onRefresh: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        GlassCard(cornerRadius = 22.dp) {
            Column(
                modifier = Modifier.padding(24.dp),
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
                    color = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onRefresh) {
                    Text("Повторить")
                }
            }
        }
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
        shape = RoundedCornerShape(14.dp),
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
private fun EmptyDayCard(hasSearchQuery: Boolean) {
    val style = LocalAppVisualStyle.current
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        cornerRadius = 20.dp,
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
                tint = if (style.isNeon) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
            )
            Text(
                text = if (hasSearchQuery) "Ничего не найдено" else "В этот день пар нет",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (hasSearchQuery) {
                    "Попробуйте изменить поисковый запрос"
                } else {
                    "Можно отдохнуть или свайпнуть на другой день"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun isSlotRunningNow(
    slot: LessonSlot,
    currentLessonId: String?,
    nowTime: LocalTime,
): Boolean {
    if (currentLessonId != null && slot.lessons.any { it.id == currentLessonId }) {
        return true
    }
    return slot.lessons.any { lesson ->
        val start = lesson.startTime
        val end = lesson.endTime
        start != null && end != null && !nowTime.isBefore(start) && nowTime.isBefore(end)
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
