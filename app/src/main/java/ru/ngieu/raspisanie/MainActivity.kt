package ru.ngieu.raspisanie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.ngieu.raspisanie.ui.ScheduleScreen
import ru.ngieu.raspisanie.ui.ScheduleViewModel
import ru.ngieu.raspisanie.ui.ScheduleViewMode
import ru.ngieu.raspisanie.ui.theme.NgieuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ScheduleViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            NgieuTheme(themeMode = state.themeMode) {
                ScheduleScreen(
                    state = state,
                    onRefresh = { viewModel.refresh(silent = false) },
                    onSelectDate = viewModel::selectDate,
                    onShiftWeek = viewModel::shiftWeek,
                    onGoToday = viewModel::goToToday,
                    onSelectWeekMode = viewModel::setWeekOverride,
                    onToggleViewMode = {
                        val next = if (state.viewMode == ScheduleViewMode.DAY) {
                            ScheduleViewMode.WEEK
                        } else {
                            ScheduleViewMode.DAY
                        }
                        viewModel.setViewMode(next)
                    },
                    onSelectTheme = viewModel::setThemeMode,
                )
            }
        }
    }
}
