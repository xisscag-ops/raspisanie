package ru.ngieu.raspisanie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.ngieu.raspisanie.ui.ScheduleScreen
import ru.ngieu.raspisanie.ui.ScheduleViewModel
import ru.ngieu.raspisanie.ui.theme.NgieuTheme

class MainActivity : ComponentActivity() {

    private var scheduleViewModel: ScheduleViewModel? = null
    private var suppressNextStopIconApply = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ScheduleViewModel = viewModel()
            scheduleViewModel = viewModel
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            LaunchedEffect(systemDark) {
                viewModel.onSystemDarkChanged(systemDark)
            }

            NgieuTheme(themeMode = state.themeMode) {
                ScheduleScreen(
                    state = state,
                    onRefresh = { viewModel.refresh(silent = false) },
                    onSelectDate = viewModel::selectDate,
                    onShiftWeek = viewModel::shiftWeek,
                    onGoToday = viewModel::goToToday,
                    onSelectWeekMode = viewModel::setWeekOverride,
                    onSelectTab = viewModel::selectTab,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onActorSearchFilterChange = viewModel::setActorSearchFilter,
                    onOpenSearch = viewModel::ensureActorsLoaded,
                    onSelectActor = viewModel::selectActor,
                    onResetToDefaultGroup = viewModel::resetToDefaultGroup,
                    onSelectTheme = viewModel::setThemeMode,
                    onSelectIconStyle = viewModel::setIconStyle,
                    onApplyIconNow = {
                        suppressNextStopIconApply = true
                        moveTaskToBack(true)
                        viewModel.applyLauncherIconNow()
                    },
                    onSelectSubgroup = viewModel::setSubgroupFilter,
                    onCheckForUpdates = { viewModel.checkForUpdates(silent = false) },
                    onInstallUpdate = {
                        suppressNextStopIconApply = true
                        viewModel.downloadAndInstallUpdate()
                    },
                    onShareSchedule = {
                        suppressNextStopIconApply = true
                        viewModel.shareDaySchedule(this)
                    },
                    onUpdateTetrisBestScore = viewModel::updateTetrisBestScore,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        suppressNextStopIconApply = false
        scheduleViewModel?.refreshActiveLauncherIcon()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations && !suppressNextStopIconApply) {
            scheduleViewModel?.applyPendingIconIfNeeded()
        }
    }
}
