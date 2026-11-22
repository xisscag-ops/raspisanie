package ru.ngieu.raspisanie.ui.share

import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.domain.WeekFilterMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ShareDaySection(
    val date: LocalDate,
    val dayTitle: String,
    val slots: List<LessonSlot>,
)

data class ShareWeekSnapshot(
    val groupName: String,
    val selectedMonday: LocalDate,
    val weekFilter: WeekFilterMode,
    val weekRangeLabel: String,
    val days: List<ShareDaySection>,
    val totalPairsCount: Int,
)

object ScheduleShareBuilder {
    private val ruLocale = Locale("ru", "RU")
    private val dayMonthFullFormatter = DateTimeFormatter.ofPattern("d MMMM", ruLocale)
    private val dayMonthShortFormatter = DateTimeFormatter.ofPattern("d MMM", ruLocale)

    fun buildWeekSnapshot(
        groupName: String,
        selectedMonday: LocalDate,
        weekFilter: WeekFilterMode,
        allLessons: List<Lesson>,
    ): ShareWeekSnapshot {
        val monday = ScheduleEngine.mondayOf(selectedMonday)
        val saturday = monday.plusDays(5)
        val rangeLabel = "${monday.format(dayMonthShortFormatter)} – ${saturday.format(dayMonthShortFormatter)}"

        val days = (0L..5L).map { offset ->
            val date = monday.plusDays(offset)
            val rawLessons = ScheduleEngine.lessonsForDay(
                allLessons = allLessons,
                targetDate = date,
                weekFilter = weekFilter,
            )
            val slots = ScheduleEngine.groupLessonsIntoSlots(rawLessons)
            ShareDaySection(
                date = date,
                dayTitle = "${fullRussianDayName(date.dayOfWeek)}, ${date.format(dayMonthFullFormatter)}",
                slots = slots,
            )
        }

        return ShareWeekSnapshot(
            groupName = groupName,
            selectedMonday = monday,
            weekFilter = weekFilter,
            weekRangeLabel = rangeLabel,
            days = days,
            totalPairsCount = days.sumOf { it.slots.size },
        )
    }

    fun formatPairsCount(count: Int): String {
        if (count == 0) return "Занятий нет"
        val mod100 = count % 100
        val mod10 = count % 10
        val word = when {
            mod100 in 11..14 -> "пар"
            mod10 == 1 -> "пара"
            mod10 in 2..4 -> "пары"
            else -> "пар"
        }
        return "$count $word"
    }

    fun fullRussianDayName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Понедельник"
        DayOfWeek.TUESDAY -> "Вторник"
        DayOfWeek.WEDNESDAY -> "Среда"
        DayOfWeek.THURSDAY -> "Четверг"
        DayOfWeek.FRIDAY -> "Пятница"
        DayOfWeek.SATURDAY -> "Суббота"
        DayOfWeek.SUNDAY -> "Воскресенье"
    }
}
