package ru.ngieu.raspisanie.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ru.ngieu.raspisanie.BuildConfig
import ru.ngieu.raspisanie.R
import ru.ngieu.raspisanie.data.ActorKind
import ru.ngieu.raspisanie.data.AppIconStyle
import ru.ngieu.raspisanie.data.AppUpdater
import ru.ngieu.raspisanie.data.ResolvedAppIcon
import ru.ngieu.raspisanie.data.ScheduleActor
import ru.ngieu.raspisanie.data.ScheduleParser
import ru.ngieu.raspisanie.data.ScheduleRepository
import ru.ngieu.raspisanie.data.SubgroupFilterMode
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.domain.WeekFilterMode
import ru.ngieu.raspisanie.ui.components.DaySelectorBottomDock
import ru.ngieu.raspisanie.ui.components.GlassCard
import ru.ngieu.raspisanie.ui.components.HorizontalBiasPagerContainer
import ru.ngieu.raspisanie.ui.components.LessonCard
import ru.ngieu.raspisanie.ui.components.LiveLessonBanner
import ru.ngieu.raspisanie.ui.components.NeonAuroraBackground
import ru.ngieu.raspisanie.ui.components.NeonTopNavBar
import ru.ngieu.raspisanie.ui.components.TetrisGameDialog
import ru.ngieu.raspisanie.ui.components.VerticalListWithHorizontalBias
import ru.ngieu.raspisanie.ui.components.WeekSelectorBar
import ru.ngieu.raspisanie.ui.components.fullDayName
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
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
    onActorSearchFilterChange: (ActorKind?) -> Unit,
    onOpenSearch: () -> Unit,
    onSelectActor: (ScheduleActor) -> Unit,
    onResetToDefaultGroup: () -> Unit,
    onSelectTheme: (ThemeModeSetting) -> Unit,
    onSelectIconStyle: (AppIconStyle) -> Unit,
    onApplyIconNow: () -> Unit,
    onSelectSubgroup: (SubgroupFilterMode) -> Unit,
    onCheckForUpdates: () -> Unit,
    onInstallUpdate: () -> Unit,
    onShareSchedule: () -> Unit,
    onUpdateTetrisBestScore: (Int) -> Unit,
) {
    var showWeekPickerSheet by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showTetrisGame by remember { mutableStateOf(false) }

    val currentLessonId = (state.liveLessonState as? LiveLessonState.InProgress)?.currentLesson?.id
    val hasAppUpdate = state.updateState is UpdateCheckState.Available ||
        state.updateState is UpdateCheckState.ReadyToInstall ||
        state.updateState is UpdateCheckState.NeedsInstallPermission

    val isOnDefaultHomeDay = !showSearchBar &&
        state.activeTab == MainNavTab.HOME &&
        state.isViewingToday

    BackHandler(enabled = !isOnDefaultHomeDay) {
        when {
            showSearchBar -> {
                showSearchBar = false
                if (state.searchQuery.isNotEmpty()) onSearchQueryChange("")
                if (state.actorSearchFilter != null) onActorSearchFilterChange(null)
                if (state.activeTab != MainNavTab.HOME && state.activeTab != MainNavTab.WEEK) {
                    onSelectTab(MainNavTab.HOME)
                }
            }
            state.activeTab != MainNavTab.HOME -> {
                onSelectTab(MainNavTab.HOME)
                if (!state.isViewingToday) {
                    onGoToday()
                }
            }
            !state.isViewingToday -> {
                onGoToday()
            }
        }
    }

    NeonAuroraBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
            ) {
                // Верхний заголовок в стиле Neon Glass
                val subgroupSuffix = if (state.effectiveSubgroupFilter != SubgroupFilterMode.ALL) {
                    " • ${state.effectiveSubgroupFilter.shortLabel}"
                } else {
                    ""
                }
                NeonTopHeader(
                    groupName = state.groupName,
                    activeActorKind = state.activeActorKind,
                    isViewingDefaultGroup = state.isViewingDefaultGroup,
                    weekSubtitle = "${state.activeWeekFilter.title} неделя$subgroupSuffix",
                    showTodayPill = !state.isViewingToday,
                    isSearchVisible = showSearchBar,
                    isSharing = state.isSharingSchedule,
                    canShare = !state.isLoading && state.allLessons.isNotEmpty(),
                    onClickWeekPicker = { showWeekPickerSheet = true },
                    onClickToday = onGoToday,
                    onClickResetGroup = onResetToDefaultGroup,
                    onClickOpenGame = { showTetrisGame = true },
                    onClickShare = onShareSchedule,
                    onToggleSearch = {
                        showSearchBar = !showSearchBar
                        if (showSearchBar) {
                            onOpenSearch()
                        } else if (state.searchQuery.isNotEmpty()) {
                            onSearchQueryChange("")
                        }
                    },
                )

                // Верхняя панель навигации (Главная, Неделя, Звонки, Настройки)
                NeonTopNavBar(
                    activeTab = state.activeTab,
                    onSelectTab = onSelectTab,
                    hasAppUpdate = hasAppUpdate,
                )

                // Раскрывающийся поиск по предметам, группам и преподавателям
                AnimatedVisibility(
                    visible = showSearchBar,
                    enter = expandVertically(tween(110)) + fadeIn(tween(110)),
                    exit = shrinkVertically(tween(90)) + fadeOut(tween(90)),
                ) {
                    NeonSearchBar(
                        query = state.searchQuery,
                        actors = state.availableActors,
                        activeActorId = state.activeActorId,
                        selectedFilter = state.actorSearchFilter,
                        isActorsLoading = state.isActorsLoading,
                        isViewingDefaultGroup = state.isViewingDefaultGroup,
                        onQueryChange = onSearchQueryChange,
                        onSelectFilter = onActorSearchFilterChange,
                        onSelectActor = { actor ->
                            onSelectActor(actor)
                            showSearchBar = false
                        },
                        onResetToDefaultGroup = {
                            onResetToDefaultGroup()
                            showSearchBar = false
                        },
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
                                    onRefresh = onRefresh,
                                )
                            }

                            MainNavTab.GROUP -> {
                                SubgroupsAndGroupTab(
                                    state = state,
                                    onSelectSubgroup = onSelectSubgroup,
                                    onRefresh = onRefresh,
                                    onResetToDefaultGroup = onResetToDefaultGroup,
                                )
                            }

                            MainNavTab.SETTINGS -> {
                                SettingsTab(
                                    state = state,
                                    onSelectTheme = onSelectTheme,
                                    onSelectIconStyle = onSelectIconStyle,
                                    onApplyIconNow = onApplyIconNow,
                                    onSelectWeekMode = onSelectWeekMode,
                                    onGoToday = onGoToday,
                                    onRefresh = onRefresh,
                                    onOpenGame = { showTetrisGame = true },
                                    onCheckForUpdates = onCheckForUpdates,
                                    onInstallUpdate = onInstallUpdate,
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
                    showTodayButton = !state.isViewingToday,
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

    if (showTetrisGame) {
        TetrisGameDialog(
            initialBestScore = state.tetrisBestScore,
            liveLessonState = state.liveLessonState,
            onUpdateBestScore = onUpdateTetrisBestScore,
            onDismiss = { showTetrisGame = false },
        )
    }
}

@Composable
private fun NeonTopHeader(
    groupName: String,
    activeActorKind: ActorKind,
    isViewingDefaultGroup: Boolean,
    weekSubtitle: String,
    showTodayPill: Boolean,
    isSearchVisible: Boolean,
    isSharing: Boolean,
    canShare: Boolean,
    onClickWeekPicker: () -> Unit,
    onClickToday: () -> Unit,
    onClickResetGroup: () -> Unit,
    onClickOpenGame: () -> Unit,
    onClickShare: () -> Unit,
    onToggleSearch: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val titleColor = when {
        style.isNeon -> NeonPalette.textPrimary
        style.isGlass -> GlassPalette.textPrimary
        else -> MaterialTheme.colorScheme.onBackground
    }
    val subtitleColor = when {
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = if (activeActorKind == ActorKind.TEACHER) groupName else "Группа $groupName",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (style.isGlass) {
                    HeaderCircleButton(
                        iconRes = R.drawable.ic_glass_game,
                        contentDescription = "Тетрис на перемене",
                        highlighted = false,
                        onClick = onClickOpenGame,
                    )
                } else {
                    HeaderVectorCircleButton(
                        imageVector = Icons.Rounded.SportsEsports,
                        contentDescription = "Тетрис на перемене",
                        onClick = onClickOpenGame,
                    )
                }
                HeaderCircleButton(
                    iconRes = R.drawable.ic_neon_share,
                    contentDescription = "Поделиться расписанием",
                    highlighted = false,
                    enabled = canShare && !isSharing,
                    isLoading = isSharing,
                    onClick = onClickShare,
                )
                HeaderCircleButton(
                    iconRes = R.drawable.ic_neon_search,
                    contentDescription = "Поиск по расписанию",
                    highlighted = isSearchVisible,
                    onClick = onToggleSearch,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClickWeekPicker)
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = weekSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = subtitleColor,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_neon_expand),
                    contentDescription = "Выбрать неделю",
                    colorFilter = when {
                        style.isNeon -> null
                        style.isGlass -> ColorFilter.tint(GlassPalette.textSecondary)
                        else -> ColorFilter.tint(subtitleColor)
                    },
                    modifier = Modifier.size(13.dp),
                )
            }

            if (showTodayPill) {
                val todayBtnBrush = when {
                    style.isNeon -> Brush.horizontalGradient(
                        colors = listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                    )
                    style.isGlass -> Brush.verticalGradient(
                        colors = listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                    )
                    else -> Brush.horizontalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                    )
                }
                val todayBtnBorder = when {
                    style.isNeon -> NeonPalette.activeDayBorder.copy(alpha = 0.72f)
                    style.isGlass -> GlassPalette.panelBorderTop
                    else -> MaterialTheme.colorScheme.primary
                }
                val todayBtnContent = when {
                    style.isNeon || style.isGlass -> Color.White
                    else -> MaterialTheme.colorScheme.onPrimary
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush = todayBtnBrush, shape = RoundedCornerShape(12.dp))
                        .border(width = 1.dp, color = todayBtnBorder, shape = RoundedCornerShape(12.dp))
                        .clickable(onClick = onClickToday)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Today,
                        contentDescription = null,
                        tint = todayBtnContent,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isViewingDefaultGroup) "Открыть сегодня" else "Сегодня",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = todayBtnContent,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (!isViewingDefaultGroup) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                style.isNeon -> Color(0x3838BDF8)
                                style.isGlass -> GlassPalette.secondaryPillBg
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                style.isNeon -> Color(0x667DD3FC)
                                style.isGlass -> Color(0x4DF4F4F4)
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(12.dp),
                        )
                        .clickable(onClick = onClickResetGroup)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "← ${ScheduleRepository.TARGET_GROUP_NAME}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            style.isNeon -> Color(0xFF7DD3FC)
                            style.isGlass -> GlassPalette.textPrimary
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderVectorCircleButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val fill = when {
        style.isNeon -> Brush.verticalGradient(listOf(Color(0xBA242D58), Color(0xB3161E41)))
        style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.panelFillTop, GlassPalette.panelFillBottom))
        else -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }
    val border = when {
        style.isNeon -> Brush.verticalGradient(listOf(NeonPalette.glassBorderTop, NeonPalette.glassBorderBottom))
        style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.panelBorderTop, GlassPalette.panelBorderBottom))
        else -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant))
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(brush = fill, shape = CircleShape)
            .border(width = 1.dp, brush = border, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = when {
                style.isNeon -> Color(0xFFC4B5FD)
                style.isGlass -> Color(0xFFF5F5F5)
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun HeaderCircleButton(
    iconRes: Int,
    contentDescription: String,
    highlighted: Boolean,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val fill = when {
        highlighted && style.isNeon -> Brush.verticalGradient(listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd))
        highlighted && style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom))
        highlighted -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primaryContainer))
        style.isNeon -> Brush.verticalGradient(listOf(Color(0xBA242D58), Color(0xB3161E41)))
        style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.panelFillTop, GlassPalette.panelFillBottom))
        else -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }
    val border = when {
        style.isNeon -> Brush.verticalGradient(listOf(NeonPalette.glassBorderTop, NeonPalette.glassBorderBottom))
        style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.panelBorderTop, GlassPalette.panelBorderBottom))
        else -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant))
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(brush = fill, shape = CircleShape)
            .border(width = 1.dp, brush = border, shape = CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = when {
                    style.isNeon -> NeonPalette.textPrimary
                    style.isGlass -> GlassPalette.textPrimary
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(17.dp),
            )
        } else {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                colorFilter = when {
                    style.isNeon -> null
                    style.isGlass -> if (iconRes == R.drawable.ic_glass_game) null else ColorFilter.tint(Color(0xFFF5F5F5))
                    else -> ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                },
                alpha = if (enabled) 1f else 0.4f,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun NeonSearchBar(
    query: String,
    actors: List<ScheduleActor>,
    activeActorId: String,
    selectedFilter: ActorKind?,
    isActorsLoading: Boolean,
    isViewingDefaultGroup: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectFilter: (ActorKind?) -> Unit,
    onSelectActor: (ScheduleActor) -> Unit,
    onResetToDefaultGroup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalAppVisualStyle.current
    val textColor = when {
        style.isNeon -> NeonPalette.textPrimary
        style.isGlass -> GlassPalette.textPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val hintColor = when {
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val groupsCount = remember(actors) { actors.count { it.kind == ActorKind.GROUP } }
    val teachersCount = remember(actors) { actors.count { it.kind == ActorKind.TEACHER } }
    val matchingActors = remember(actors, query, selectedFilter) {
        if (query.isBlank() && selectedFilter == null) {
            emptyList()
        } else {
            ScheduleParser.searchActors(actors, query, selectedFilter).take(40)
        }
    }
    val showActorsPanel = query.isNotBlank() || selectedFilter != null || !isViewingDefaultGroup

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
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
                    colorFilter = when {
                        style.isNeon -> null
                        style.isGlass -> ColorFilter.tint(GlassPalette.textSecondary)
                        else -> ColorFilter.tint(hintColor)
                    },
                    modifier = Modifier.size(18.dp),
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Группа, преподаватель или предмет...",
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
                        cursorBrush = SolidColor(
                            when {
                                style.isNeon -> Color(0xFF38BDF8)
                                style.isGlass -> Color.White
                                else -> MaterialTheme.colorScheme.primary
                            },
                        ),
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

        // Чипы быстрого переключения: Все / Группы / Преподаватели / Моя группа
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchFilterChip(
                label = "Все",
                selected = selectedFilter == null,
                onClick = { onSelectFilter(null) },
                modifier = Modifier.weight(1f),
            )
            SearchFilterChip(
                label = if (groupsCount > 0) "Группы ($groupsCount)" else "Группы",
                selected = selectedFilter == ActorKind.GROUP,
                onClick = {
                    onSelectFilter(if (selectedFilter == ActorKind.GROUP) null else ActorKind.GROUP)
                },
                modifier = Modifier.weight(1f),
            )
            SearchFilterChip(
                label = if (teachersCount > 0) "Препод. ($teachersCount)" else "Преподаватели",
                selected = selectedFilter == ActorKind.TEACHER,
                onClick = {
                    onSelectFilter(if (selectedFilter == ActorKind.TEACHER) null else ActorKind.TEACHER)
                },
                modifier = Modifier.weight(1f),
            )
        }

        if (showActorsPanel) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    if (!isViewingDefaultGroup) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onResetToDefaultGroup)
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Вернуться к своей группе (${ScheduleRepository.TARGET_GROUP_NAME})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    style.isNeon -> Color(0xFF7DD3FC)
                                    style.isGlass -> GlassPalette.textPrimary
                                    else -> MaterialTheme.colorScheme.primary
                                },
                            )
                        }
                        if (matchingActors.isNotEmpty() || isActorsLoading) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = when {
                                    style.isNeon -> Color(0x24FFFFFF)
                                    style.isGlass -> Color(0x33F4F4F4)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                            )
                        }
                    }

                    when {
                        isActorsLoading && actors.isEmpty() -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "Загружаем список групп и преподавателей...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = hintColor,
                                )
                            }
                        }

                        matchingActors.isEmpty() && (query.isNotBlank() || selectedFilter != null) -> {
                            Text(
                                text = if (query.isNotBlank()) {
                                    "Группы и преподаватели по запросу не найдены"
                                } else {
                                    "Список пока пуст — потяните экран вниз для обновления"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = hintColor,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            )
                        }

                        matchingActors.isNotEmpty() -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 220.dp),
                            ) {
                                items(
                                    items = matchingActors,
                                    key = { it.id },
                                ) { actor ->
                                    val isCurrent = actor.id == activeActorId
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectActor(actor) }
                                            .padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Image(
                                            painter = painterResource(
                                                id = if (actor.kind == ActorKind.GROUP) {
                                                    R.drawable.ic_neon_group
                                                } else {
                                                    R.drawable.ic_neon_teacher
                                                },
                                            ),
                                            contentDescription = null,
                                            colorFilter = when {
                                                style.isNeon -> null
                                                style.isGlass -> ColorFilter.tint(GlassPalette.iconTint)
                                                else -> ColorFilter.tint(hintColor)
                                            },
                                            modifier = Modifier.size(18.dp),
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = actor.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                color = when {
                                                    isCurrent && style.isNeon -> Color(0xFF7DD3FC)
                                                    isCurrent && style.isGlass -> Color.White
                                                    isCurrent -> MaterialTheme.colorScheme.primary
                                                    else -> textColor
                                                },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = if (actor.kind == ActorKind.GROUP) "Группа" else "Преподаватель",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = hintColor,
                                            )
                                        }
                                        if (isCurrent) {
                                            Text(
                                                text = "Открыто",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (style.isTranslucent) Color(0xFF34D399) else MaterialTheme.colorScheme.primary,
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
    }
}

@Composable
private fun SearchFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalAppVisualStyle.current
    val fillBrush = when {
        selected && style.isNeon -> Brush.verticalGradient(listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd))
        selected && style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom))
        selected -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
        else -> null
    }
    val labelColor = when {
        selected -> Color.White
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    GlassCard(
        modifier = modifier,
        cornerRadius = 11.dp,
        fillBrush = fillBrush,
        onClick = onClick,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun HomeScheduleTab(
    state: ScheduleUiState,
    currentLessonId: String?,
    onSelectDate: (LocalDate) -> Unit,
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
            )
        }
    }
}

@Composable
private fun DayHorizontalPager(
    state: ScheduleUiState,
    currentLessonId: String?,
    onSelectDate: (LocalDate) -> Unit,
) {
    val latestSelectedDate by rememberUpdatedState(state.selectedDate)
    val initialPage = remember {
        ScheduleEngine.dateToDayIndex(state.anchorMonday, state.selectedDate)
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { ScheduleEngine.TOTAL_DAY_PAGES },
    )
    val coroutineScope = rememberCoroutineScope()
    var lastPagerDate by remember { mutableStateOf(state.selectedDate) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val pageDate = ScheduleEngine.dayIndexToDate(state.anchorMonday, page)
                lastPagerDate = pageDate
                if (pageDate != latestSelectedDate) {
                    onSelectDate(pageDate)
                }
            }
    }

    LaunchedEffect(state.selectedDate) {
        if (state.selectedDate == lastPagerDate) return@LaunchedEffect
        lastPagerDate = state.selectedDate
        val targetPage = ScheduleEngine.dateToDayIndex(state.anchorMonday, state.selectedDate)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    HorizontalBiasPagerContainer(
        isPagerScrolling = { pagerState.isScrollInProgress },
        onEnsureSettled = {
            coroutineScope.launch {
                if (!pagerState.isScrollInProgress && pagerState.currentPageOffsetFraction != 0f) {
                    pagerState.animateScrollToPage(pagerState.targetPage)
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { gateState, allowVerticalScroll ->
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
            val activeSubgroup = state.effectiveSubgroupFilter
            val daySlots = remember(
                state.allLessons,
                state.lessonsByWeekDay,
                state.searchQuery,
                activeSubgroup,
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
                        subgroupFilter = activeSubgroup,
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

            val listState = remember(pageDate) { LazyListState() }
            val isOffscreen by remember(pagerState, pageIndex) {
                derivedStateOf {
                    !pagerState.isScrollInProgress && pagerState.currentPage != pageIndex
                }
            }
            LaunchedEffect(isOffscreen) {
                if (isOffscreen && (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0)) {
                    listState.scrollToItem(0)
                }
            }

            VerticalListWithHorizontalBias(gateState = gateState) {
                DayLessonsList(
                    pageDate = pageDate,
                    todayDate = state.todayDate,
                    daySlots = daySlots,
                    isViewingToday = isViewingToday,
                    liveLessonState = state.liveLessonState,
                    progressSummary = pageProgress,
                    currentLessonId = currentLessonId,
                    nowTime = state.nowTime,
                    themeMode = state.themeMode,
                    hasSearchQuery = state.searchQuery.isNotBlank(),
                    showGroupsInsteadOfTeachers = state.activeActorKind == ActorKind.TEACHER,
                    userScrollEnabled = allowVerticalScroll,
                    listState = listState,
                )
            }
        }
    }
}

@Composable
private fun DayLessonsList(
    pageDate: LocalDate,
    todayDate: LocalDate,
    daySlots: List<LessonSlot>,
    isViewingToday: Boolean,
    liveLessonState: LiveLessonState,
    progressSummary: DayProgressSummary,
    currentLessonId: String?,
    nowTime: LocalTime,
    themeMode: ThemeModeSetting,
    hasSearchQuery: Boolean,
    showGroupsInsteadOfTeachers: Boolean,
    userScrollEnabled: Boolean,
    listState: LazyListState,
) {
    val style = LocalAppVisualStyle.current
    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val isPageToday = pageDate == todayDate
    val isPastDay = pageDate.isBefore(todayDate)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = userScrollEnabled,
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
                    daySlots = daySlots,
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
                    color = when {
                        style.isNeon -> NeonPalette.textSecondary
                        style.isGlass -> GlassPalette.textSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    text = pairsCountLabel(daySlots.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        style.isNeon -> NeonPalette.textSecondary
                        style.isGlass -> GlassPalette.textSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
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
                    isToday = isPageToday,
                    isPastDay = isPastDay,
                    nowTime = nowTime,
                    themeMode = themeMode,
                    isFirst = index == 0,
                    isLast = index == daySlots.lastIndex,
                    showGroupsInsteadOfTeachers = showGroupsInsteadOfTeachers,
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
            )
        }
    }
}

@Composable
private fun WeekHorizontalPager(
    state: ScheduleUiState,
    currentLessonId: String?,
    onSelectDate: (LocalDate) -> Unit,
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
    val coroutineScope = rememberCoroutineScope()
    var lastPagerMonday by remember { mutableStateOf(state.selectedMonday) }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val pageMonday = ScheduleEngine.weekIndexToMonday(state.anchorMonday, page)
                lastPagerMonday = pageMonday
                val currentMonday = ScheduleEngine.mondayOf(latestSelectedDate)
                if (pageMonday != currentMonday) {
                    val dayOffset = (latestSelectedDate.dayOfWeek.value - 1).coerceIn(0, 5).toLong()
                    onSelectDate(pageMonday.plusDays(dayOffset))
                }
            }
    }

    LaunchedEffect(state.selectedMonday) {
        if (state.selectedMonday == lastPagerMonday) return@LaunchedEffect
        lastPagerMonday = state.selectedMonday
        val targetPage = ScheduleEngine.mondayToWeekIndex(state.anchorMonday, state.selectedMonday)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    HorizontalBiasPagerContainer(
        isPagerScrolling = { pagerState.isScrollInProgress },
        onEnsureSettled = {
            coroutineScope.launch {
                if (!pagerState.isScrollInProgress && pagerState.currentPageOffsetFraction != 0f) {
                    pagerState.animateScrollToPage(pagerState.targetPage)
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { gateState, allowVerticalScroll ->
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
            val activeSubgroup = state.effectiveSubgroupFilter
            val slotsByDay = remember(
                state.allLessons,
                state.lessonsByWeekDay,
                state.searchQuery,
                activeSubgroup,
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
                            subgroupFilter = activeSubgroup,
                        ).filter(state::matchesSearch)
                    }
                }
                map.mapValues { (_, list) -> ScheduleEngine.groupLessonsIntoSlots(list) }
            }

            val isCurrentWeek = pageMonday == ScheduleEngine.mondayOf(state.todayDate)
            val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val listState = remember(pageMonday) { LazyListState() }
            val isOffscreen by remember(pagerState, pageIndex) {
                derivedStateOf {
                    !pagerState.isScrollInProgress && pagerState.currentPage != pageIndex
                }
            }
            LaunchedEffect(isOffscreen) {
                if (isOffscreen && (listState.firstVisibleItemIndex != 0 || listState.firstVisibleItemScrollOffset != 0)) {
                    listState.scrollToItem(0)
                }
            }

            VerticalListWithHorizontalBias(gateState = gateState) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = allowVerticalScroll,
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
                        daySlots = emptyList(),
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }
            }

            pageWeekDays.forEach { date ->
                val daySlots = slotsByDay[date].orEmpty()
                val isToday = date == state.todayDate
                val isPastDay = date.isBefore(state.todayDate)

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
                                    isToday && style.isGlass -> Color.White
                                    isToday -> MaterialTheme.colorScheme.primary
                                    style.isNeon -> NeonPalette.textPrimary
                                    style.isGlass -> GlassPalette.textPrimary
                                    else -> MaterialTheme.colorScheme.onBackground
                                },
                            )
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                style.isNeon -> Color(0x3838BDF8)
                                                style.isGlass -> GlassPalette.typePillBg
                                                else -> MaterialTheme.colorScheme.primaryContainer
                                            },
                                        )
                                        .padding(horizontal = 7.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "Сегодня",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = when {
                                            style.isNeon -> Color(0xFF7DD3FC)
                                            style.isGlass -> GlassPalette.typePillText
                                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                                        },
                                    )
                                }
                            }
                        }
                        Text(
                            text = pairsCountLabel(daySlots.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                style.isNeon -> NeonPalette.textSecondary
                                style.isGlass -> GlassPalette.textSecondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
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
                                color = when {
                                    style.isNeon -> NeonPalette.textSecondary
                                    style.isGlass -> GlassPalette.textSecondary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
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
                            isToday = isToday,
                            isPastDay = isPastDay,
                            nowTime = state.nowTime,
                            themeMode = state.themeMode,
                            isFirst = index == 0,
                            isLast = index == daySlots.lastIndex,
                            showGroupsInsteadOfTeachers = state.activeActorKind == ActorKind.TEACHER,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
            }
        }
    }
}

@Composable
private fun SubgroupsAndGroupTab(
    state: ScheduleUiState,
    onSelectSubgroup: (SubgroupFilterMode) -> Unit,
    onRefresh: () -> Unit,
    onResetToDefaultGroup: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val titleColor = when {
        style.isNeon -> NeonPalette.textPrimary
        style.isGlass -> GlassPalette.textPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val subColor = when {
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val updatedLabel = state.updatedAtEpochMillis?.let { millis ->
        val formatted = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.of("Europe/Moscow"))
            .format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm"))
        "Синхронизация: $formatted (МСК)"
    } ?: "Ожидается синхронизация"
    val navBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val subgroupOptions = listOf(
        SubgroupFilterMode.FIRST,
        SubgroupFilterMode.SECOND,
        SubgroupFilterMode.ALL,
    )

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
                glowColor = when {
                    style.isNeon -> Color(0xFF38BDF8)
                    style.isGlass -> Color.White
                    else -> null
                },
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
                                    if (style.isGlass) {
                                        Brush.verticalGradient(
                                            listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                                        )
                                    },
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
                                text = "Отображение подгрупп",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                            )
                            Text(
                                text = "Выберите свою подгруппу или показ обеих сразу",
                                style = MaterialTheme.typography.bodySmall,
                                color = subColor,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        subgroupOptions.forEach { mode ->
                            val selected = state.subgroupFilter == mode
                            val pillBrush = when {
                                selected && style.isNeon -> Brush.verticalGradient(
                                    listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                                )
                                selected && style.isGlass -> Brush.verticalGradient(
                                    listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                                )
                                selected -> Brush.verticalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                                )
                                else -> null
                            }
                            val borderBrush = when {
                                selected && style.isNeon -> Brush.linearGradient(
                                    listOf(Color(0x99E9D5FF), Color(0x667DD3FC)),
                                )
                                selected && style.isGlass -> Brush.linearGradient(
                                    listOf(GlassPalette.panelBorderTop, GlassPalette.panelBorderBottom),
                                )
                                selected -> Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                                )
                                style.isNeon -> Brush.linearGradient(
                                    listOf(NeonPalette.glassBorderTop, NeonPalette.glassBorderBottom),
                                )
                                style.isGlass -> Brush.linearGradient(
                                    listOf(GlassPalette.cardBorderTop, GlassPalette.cardBorderBottom),
                                )
                                else -> Brush.linearGradient(
                                    listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant),
                                )
                            }

                            GlassCard(
                                modifier = Modifier.weight(1f),
                                cornerRadius = 14.dp,
                                fillBrush = pillBrush,
                                borderBrush = borderBrush,
                                borderWidth = if (selected) 1.4.dp else 1.dp,
                                onClick = { onSelectSubgroup(mode) },
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
                                        .padding(vertical = 12.dp, horizontal = 6.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val hintText = when {
                        state.activeActorKind == ActorKind.TEACHER ->
                            "Сейчас открыто расписание преподавателя — для него показываются все пары, а ваш выбор сохранится для групп."
                        state.subgroupFilter == SubgroupFilterMode.ALL ->
                            "Показываются пары обеих подгрупп. Выберите 1 или 2 подгруппу, чтобы скрыть лишние карточки."
                        else ->
                            "Включена ${state.subgroupFilter.label.lowercase()}: пары другой подгруппы скрыты, а общие лекции и пары всей группы видны всегда."
                    }
                    Text(
                        text = hintText,
                        style = MaterialTheme.typography.bodySmall,
                        color = subColor,
                    )
                }
            }
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                glowColor = when {
                    style.isNeon -> Color(0xFF7C3AED)
                    style.isGlass -> Color.White
                    else -> null
                },
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
                                    if (style.isGlass) {
                                        Brush.verticalGradient(
                                            listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                                        )
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (state.activeActorKind == ActorKind.TEACHER) {
                                        R.drawable.ic_neon_teacher
                                    } else {
                                        R.drawable.ic_neon_group
                                    },
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.activeActorKind == ActorKind.TEACHER) {
                                    state.groupName
                                } else {
                                    "Группа ${state.groupName}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = titleColor,
                            )
                            Text(
                                text = if (state.activeActorKind == ActorKind.TEACHER) {
                                    "Преподаватель • Княгининский университет (НГИЭУ)"
                                } else {
                                    "Княгининский университет (НГИЭУ)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = subColor,
                            )
                        }
                    }

                    if (!state.isViewingDefaultGroup) {
                        Spacer(modifier = Modifier.height(10.dp))
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 12.dp,
                            onClick = onResetToDefaultGroup,
                        ) {
                            Text(
                                text = "Вернуться к группе ${ScheduleRepository.TARGET_GROUP_NAME}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    style.isNeon -> Color(0xFF7DD3FC)
                                    style.isGlass -> GlassPalette.textPrimary
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
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
                                tint = when {
                                    style.isNeon -> Color(0xFF38BDF8)
                                    style.isGlass -> GlassPalette.textPrimary
                                    else -> MaterialTheme.colorScheme.primary
                                },
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsTab(
    state: ScheduleUiState,
    onSelectTheme: (ThemeModeSetting) -> Unit,
    onSelectIconStyle: (AppIconStyle) -> Unit,
    onApplyIconNow: () -> Unit,
    onSelectWeekMode: (WeekFilterMode) -> Unit,
    onGoToday: () -> Unit,
    onRefresh: () -> Unit,
    onOpenGame: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val systemDark = isSystemInDarkTheme()
    val resolvedIcon = remember(state.themeMode, state.iconStyle, systemDark) {
        AppIconManager.resolveIcon(
            themeMode = state.themeMode,
            iconStyle = state.iconStyle,
            isSystemDark = systemDark,
        )
    }
    val titleColor = when {
        style.isNeon -> NeonPalette.textPrimary
        style.isGlass -> GlassPalette.textPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val subColor = when {
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
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
                            painter = painterResource(
                                id = if (style.isGlass) R.drawable.ic_glass_themes else R.drawable.ic_neon_themes,
                            ),
                            contentDescription = null,
                            colorFilter = when {
                                style.isNeon -> null
                                style.isGlass -> ColorFilter.tint(GlassPalette.textPrimary)
                                else -> ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            },
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
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ThemeModeSetting.entries.forEach { mode ->
                            val selected = mode == state.themeMode
                            val pillBrush = when {
                                selected && style.isNeon -> Brush.verticalGradient(listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd))
                                selected && style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom))
                                selected -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                                else -> null
                            }

                            GlassCard(
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
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            AppIconSelectorCard(
                iconStyle = state.iconStyle,
                resolvedIcon = resolvedIcon,
                activeLauncherIcon = state.activeLauncherIcon,
                iconSyncError = state.iconSyncError,
                titleColor = titleColor,
                subColor = subColor,
                onSelectIconStyle = onSelectIconStyle,
                onApplyIconNow = onApplyIconNow,
            )
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
                            val pillBrush = when {
                                selected && style.isNeon -> Brush.verticalGradient(listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd))
                                selected && style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom))
                                selected -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                                else -> null
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
                                    tint = when {
                                        style.isNeon -> Color(0xFF38BDF8)
                                        style.isGlass -> GlassPalette.textPrimary
                                        else -> MaterialTheme.colorScheme.primary
                                    },
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
                                    tint = when {
                                        style.isNeon -> Color(0xFFA855F7)
                                        style.isGlass -> GlassPalette.textPrimary
                                        else -> MaterialTheme.colorScheme.primary
                                    },
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
                glowColor = when {
                    style.isNeon -> Color(0xFF8B5CF6)
                    style.isGlass -> Color.White
                    else -> null
                },
                onClick = onOpenGame,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (style.isGlass) {
                                    Brush.verticalGradient(
                                        listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                                    )
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SportsEsports,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (style.isGlass) "Стеклянный Тетрис" else "Неоновый Тетрис",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                        )
                        Text(
                            text = if (state.tetrisBestScore > 0) {
                                "Игра на перемене • Рекорд: ${state.tetrisBestScore}"
                            } else {
                                "Мини-игра для перерывов между парами"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = subColor,
                        )
                    }
                    Text(
                        text = "Играть",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            style.isNeon -> Color(0xFF7DD3FC)
                            style.isGlass -> GlassPalette.textPrimary
                            else -> MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }
        }

        item {
            AppUpdateCard(
                updateState = state.updateState,
                onCheckForUpdates = onCheckForUpdates,
                onInstallUpdate = onInstallUpdate,
            )
        }

        item {
            var bellsExpanded by remember { mutableStateOf(false) }
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
                onClick = { bellsExpanded = !bellsExpanded },
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Расписание звонков",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_neon_expand),
                            contentDescription = if (bellsExpanded) "Свернуть" else "Развернуть",
                            colorFilter = when {
                                style.isNeon -> null
                                style.isGlass -> ColorFilter.tint(GlassPalette.textSecondary)
                                else -> ColorFilter.tint(subColor)
                            },
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    AnimatedVisibility(visible = bellsExpanded) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            ScheduleEngine.standardBells.forEachIndexed { index, bell ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "${bell.pairNumber} пара",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = titleColor,
                                    )
                                    Text(
                                        text = "${bell.start} – ${bell.end}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = subColor,
                                    )
                                }
                                if (index < ScheduleEngine.standardBells.lastIndex) {
                                    HorizontalDivider(
                                        color = when {
                                            style.isNeon -> Color(0x1EFFFFFF)
                                            style.isGlass -> Color(0x2EF4F4F4)
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        },
                                    )
                                }
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

@Composable
private fun AppIconSelectorCard(
    iconStyle: AppIconStyle,
    resolvedIcon: ResolvedAppIcon,
    activeLauncherIcon: ResolvedAppIcon?,
    iconSyncError: String?,
    titleColor: Color,
    subColor: Color,
    onSelectIconStyle: (AppIconStyle) -> Unit,
    onApplyIconNow: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val rows = remember { AppIconStyle.entries.chunked(3) }
    val hasPendingChange = activeLauncherIcon != null && activeLauncherIcon != resolvedIcon

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(id = iconPreviewRes(resolvedIcon)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.24f),
                            shape = RoundedCornerShape(8.dp),
                        ),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Иконка приложения",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                    )
                    Text(
                        text = if (iconStyle == AppIconStyle.AUTO) {
                            "Автоматически по теме • сейчас: ${resolvedIcon.label}"
                        } else {
                            "Выбрана вручную: ${iconStyle.label}"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = subColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowItems.forEach { option ->
                            val selected = option == iconStyle
                            val pillBrush = when {
                                selected && style.isNeon -> Brush.verticalGradient(
                                    listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                                )
                                selected && style.isGlass -> Brush.verticalGradient(
                                    listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                                )
                                selected -> Brush.verticalGradient(
                                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                                )
                                else -> null
                            }
                            val previewTarget = when (option) {
                                AppIconStyle.AUTO -> resolvedIcon
                                AppIconStyle.NEON -> ResolvedAppIcon.NEON
                                AppIconStyle.GLASS -> ResolvedAppIcon.GLASS
                                AppIconStyle.LIGHT -> ResolvedAppIcon.LIGHT
                                AppIconStyle.DARK -> ResolvedAppIcon.DARK
                                AppIconStyle.CLASSIC -> ResolvedAppIcon.CLASSIC
                            }

                            GlassCard(
                                modifier = Modifier.weight(1f),
                                cornerRadius = 14.dp,
                                fillBrush = pillBrush,
                                onClick = { onSelectIconStyle(option) },
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(modifier = Modifier.size(44.dp)) {
                                        Image(
                                            painter = painterResource(id = iconPreviewRes(previewTarget)),
                                            contentDescription = option.label,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(11.dp))
                                                .border(
                                                    width = if (selected) 1.dp else 0.5.dp,
                                                    color = if (selected) {
                                                        Color.White.copy(alpha = 0.7f)
                                                    } else {
                                                        Color.Gray.copy(alpha = 0.3f)
                                                    },
                                                    shape = RoundedCornerShape(11.dp),
                                                ),
                                        )
                                        if (option == AppIconStyle.AUTO) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (style.isGlass) {
                                                            Color(0xFF2B2B2B)
                                                        } else {
                                                            MaterialTheme.colorScheme.primary
                                                        },
                                                    )
                                                    .border(1.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(10.dp),
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = option.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color.White else subColor,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            if (iconSyncError != null) {
                Text(
                    text = "Ошибка смены иконки: $iconSyncError",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            } else if (hasPendingChange) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "Применится при сворачивании приложения",
                        style = MaterialTheme.typography.bodySmall,
                        color = subColor,
                        modifier = Modifier.weight(1f),
                    )
                    GlassCard(
                        cornerRadius = 10.dp,
                        fillBrush = when {
                            style.isNeon -> Brush.verticalGradient(
                                listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                            )
                            style.isGlass -> Brush.verticalGradient(
                                listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                            )
                            else -> Brush.verticalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                            )
                        },
                        onClick = onApplyIconNow,
                    ) {
                        Text(
                            text = "Применить",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        )
                    }
                }
            } else {
                Text(
                    text = "В режиме «Авто» иконка меняется вместе с темой.",
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor.copy(alpha = 0.85f),
                )
            }
        }
    }
}

private fun iconPreviewRes(icon: ResolvedAppIcon): Int {
    return when (icon) {
        ResolvedAppIcon.NEON -> R.drawable.ic_icon_preview_neon
        ResolvedAppIcon.GLASS -> R.drawable.ic_icon_preview_glass
        ResolvedAppIcon.LIGHT -> R.drawable.ic_icon_preview_light
        ResolvedAppIcon.DARK -> R.drawable.ic_icon_preview_dark
        ResolvedAppIcon.CLASSIC -> R.drawable.ic_launcher
    }
}

@Composable
private fun AppUpdateCard(
    updateState: UpdateCheckState,
    onCheckForUpdates: () -> Unit,
    onInstallUpdate: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val titleColor = when {
        style.isNeon -> NeonPalette.textPrimary
        style.isGlass -> GlassPalette.textPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val subColor = when {
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val accentColor = when {
        style.isNeon -> Color(0xFF2DD4BF)
        style.isGlass -> GlassPalette.textPrimary
        else -> MaterialTheme.colorScheme.primary
    }

    val isAvailableOrReady = updateState is UpdateCheckState.Available ||
        updateState is UpdateCheckState.ReadyToInstall ||
        updateState is UpdateCheckState.NeedsInstallPermission

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        glowColor = if (style.isTranslucent && isAvailableOrReady) Color(0xFF14B8A6) else null,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.SystemUpdate,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Обновление приложения",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (updateState) {
                UpdateCheckState.Idle -> {
                    Text(
                        text = "Установлена версия ${BuildConfig.VERSION_NAME}. Обновления загружаются напрямую из GitHub Releases.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subColor,
                    )
                }

                UpdateCheckState.Checking -> {
                    Text(
                        text = "Проверяем новые версии на GitHub...",
                        style = MaterialTheme.typography.bodySmall,
                        color = subColor,
                    )
                }

                UpdateCheckState.UpToDate -> {
                    Text(
                        text = "Установлена актуальная версия ${BuildConfig.VERSION_NAME}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = subColor,
                    )
                }

                is UpdateCheckState.Available -> {
                    val sizeMb = updateState.info.sizeBytes / (1024f * 1024f)
                    val sizeText = if (sizeMb > 0f) " (${String.format("%.1f", sizeMb)} МБ)" else ""
                    Text(
                        text = "Доступна новая версия ${updateState.info.versionName}$sizeText",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                    )
                    if (updateState.info.releaseNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = updateState.info.releaseNotes.take(240),
                            style = MaterialTheme.typography.bodySmall,
                            color = subColor,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                is UpdateCheckState.Downloading -> {
                    Text(
                        text = "Загружаем версию ${updateState.info.versionName} (${(updateState.progress * 100).toInt()}%)...",
                        style = MaterialTheme.typography.bodySmall,
                        color = titleColor,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (updateState.progress > 0f) {
                        LinearProgressIndicator(
                            progress = { updateState.progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = accentColor,
                            trackColor = if (style.isTranslucent) Color(0x33FFFFFF) else MaterialTheme.colorScheme.surfaceVariant,
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = accentColor,
                            trackColor = if (style.isTranslucent) Color(0x33FFFFFF) else MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }

                is UpdateCheckState.ReadyToInstall -> {
                    Text(
                        text = "Версия ${updateState.info.versionName} загружена. Нажмите кнопку ниже, чтобы установить обновление поверх текущей версии.",
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                    )
                }

                is UpdateCheckState.NeedsInstallPermission -> {
                    Text(
                        text = "Разрешите установку из этого источника в открывшихся настройках Android, затем нажмите «Установить».",
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor,
                    )
                }

                is UpdateCheckState.Error -> {
                    Text(
                        text = updateState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val canCheck = updateState !is UpdateCheckState.Checking && updateState !is UpdateCheckState.Downloading
                GlassCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 12.dp,
                    onClick = if (canCheck) onCheckForUpdates else null,
                ) {
                    Text(
                        text = if (updateState is UpdateCheckState.Checking) "Проверка..." else "Проверить",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                    )
                }

                if (isAvailableOrReady) {
                    val installBrush = when {
                        style.isNeon -> Brush.verticalGradient(listOf(Color(0xFF14B8A6), Color(0xFF0F766E)))
                        style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom))
                        else -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                    }
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 12.dp,
                        fillBrush = installBrush,
                        onClick = onInstallUpdate,
                    ) {
                        Text(
                            text = when (updateState) {
                                is UpdateCheckState.ReadyToInstall,
                                is UpdateCheckState.NeedsInstallPermission,
                                -> "Установить"
                                else -> "Скачать и обновить"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                        )
                    }
                } else if (updateState is UpdateCheckState.Error) {
                    val uriHandler = LocalUriHandler.current
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        cornerRadius = 12.dp,
                        onClick = {
                            runCatching { uriHandler.openUri(AppUpdater.GITHUB_RELEASES_PAGE_URL) }
                        },
                    ) {
                        Text(
                            text = "Открыть GitHub",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                        )
                    }
                }
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
                color = when {
                    style.isNeon -> Color(0xFFA855F7)
                    style.isGlass -> GlassPalette.textPrimary
                    else -> MaterialTheme.colorScheme.primary
                },
            )
            Text(
                text = "Загружаем расписание $groupName...",
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    style.isNeon -> NeonPalette.textSecondary
                    style.isGlass -> GlassPalette.textSecondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
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
                    color = when {
                        style.isNeon -> NeonPalette.textPrimary
                        style.isGlass -> GlassPalette.textPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        style.isNeon -> NeonPalette.textSecondary
                        style.isGlass -> GlassPalette.textSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
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
                tint = when {
                    style.isNeon -> Color(0xFF38BDF8)
                    style.isGlass -> GlassPalette.textPrimary
                    else -> MaterialTheme.colorScheme.primary
                },
            )
            Text(
                text = if (hasSearchQuery) "Ничего не найдено" else "В этот день пар нет",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    style.isNeon -> NeonPalette.textPrimary
                    style.isGlass -> GlassPalette.textPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = if (hasSearchQuery) {
                    "Попробуйте изменить поисковый запрос"
                } else {
                    "Можно отдохнуть или свайпнуть на другой день"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    style.isNeon -> NeonPalette.textSecondary
                    style.isGlass -> GlassPalette.textSecondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
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
