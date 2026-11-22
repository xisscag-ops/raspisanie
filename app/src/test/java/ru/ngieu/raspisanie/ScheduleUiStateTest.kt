package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.ngieu.raspisanie.data.LessonType
import ru.ngieu.raspisanie.domain.WeekFilterMode
import ru.ngieu.raspisanie.ui.ScheduleUiState
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors
import java.time.LocalDate

class ScheduleUiStateTest {

    @Test
    fun `isViewingToday tracks weekday selection and manual week override`() {
        val monday = LocalDate.of(2026, 11, 23)
        val tuesday = LocalDate.of(2026, 11, 24)

        val todayState = ScheduleUiState(
            todayDate = monday,
            selectedDate = monday,
            manualWeekOverride = null,
        )
        assertTrue(todayState.isViewingToday)

        val otherDayState = todayState.copy(selectedDate = tuesday)
        assertFalse(otherDayState.isViewingToday)

        val overriddenWeekState = todayState.copy(manualWeekOverride = WeekFilterMode.LOWER)
        assertFalse(overriddenWeekState.isViewingToday)
    }

    @Test
    fun `isViewingToday treats monday as default display date on sunday`() {
        val sunday = LocalDate.of(2026, 11, 22)
        val monday = LocalDate.of(2026, 11, 23)
        val tuesday = LocalDate.of(2026, 11, 24)

        val sundayDefaultState = ScheduleUiState(
            todayDate = sunday,
            selectedDate = monday,
            manualWeekOverride = null,
        )
        assertEquals(monday, sundayDefaultState.defaultDisplayDate)
        assertTrue(sundayDefaultState.isViewingToday)

        val sundayOtherDay = sundayDefaultState.copy(selectedDate = tuesday)
        assertFalse(sundayOtherDay.isViewingToday)
    }

    @Test
    fun `lessonTypeColor uses unified color for lecture and practice`() {
        assertEquals(
            ScheduleAccentColors.lessonTypeColor(LessonType.LECTURE, isDark = true),
            ScheduleAccentColors.lessonTypeColor(LessonType.PRACTICE, isDark = true),
        )
        assertEquals(
            ScheduleAccentColors.lessonTypeColor(LessonType.LECTURE, isDark = false),
            ScheduleAccentColors.lessonTypeColor(LessonType.PRACTICE, isDark = false),
        )
    }
}
