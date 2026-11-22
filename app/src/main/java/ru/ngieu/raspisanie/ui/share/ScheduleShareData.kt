package ru.ngieu.raspisanie.ui.share

import ru.ngieu.raspisanie.data.ActorKind
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.SubgroupFilterMode
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.domain.WeekFilterMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ShareDaySnapshot(
    val actorTitle: String,
    val actorBadgeName: String,
    val actorKind: ActorKind,
    val date: LocalDate,
    val dayTitle: String,
    val weekFilter: WeekFilterMode,
    val slots: List<LessonSlot>,
    val pairsCount: Int,
)

object ScheduleShareBuilder {
    private val ruLocale = Locale("ru", "RU")
    private val dayMonthFullFormatter = DateTimeFormatter.ofPattern("d MMMM", ruLocale)

    fun buildDaySnapshot(
        actorName: String,
        actorKind: ActorKind,
        selectedDate: LocalDate,
        weekFilter: WeekFilterMode,
        allLessons: List<Lesson>,
        subgroupFilter: SubgroupFilterMode = SubgroupFilterMode.ALL,
    ): ShareDaySnapshot {
        val rawLessons = ScheduleEngine.lessonsForDay(
            allLessons = allLessons,
            targetDate = selectedDate,
            weekFilter = weekFilter,
            subgroupFilter = subgroupFilter,
        )
        val slots = ScheduleEngine.groupLessonsIntoSlots(rawLessons)
        val title = when (actorKind) {
            ActorKind.GROUP -> "Группа $actorName"
            ActorKind.TEACHER -> actorName
        }

        return ShareDaySnapshot(
            actorTitle = title,
            actorBadgeName = actorName,
            actorKind = actorKind,
            date = selectedDate,
            dayTitle = "${fullRussianDayName(selectedDate.dayOfWeek)}, ${selectedDate.format(dayMonthFullFormatter)}",
            weekFilter = weekFilter,
            slots = slots,
            pairsCount = slots.size,
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
