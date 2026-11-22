package ru.ngieu.raspisanie.domain

import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.WeekParity
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class WeekFilterMode(val title: String) {
    UPPER("Верхняя"),
    LOWER("Нижняя"),
}

sealed interface LiveLessonState {
    data object NoLessonsToday : LiveLessonState
    data class BeforeFirstLesson(
        val nextLesson: Lesson,
        val minutesUntilStart: Long,
    ) : LiveLessonState

    data class InProgress(
        val currentLesson: Lesson,
        val minutesRemaining: Long,
        val progress: Float,
    ) : LiveLessonState

    data class OnBreak(
        val nextLesson: Lesson,
        val minutesUntilStart: Long,
    ) : LiveLessonState

    data object FinishedForToday : LiveLessonState
}

data class BellSlot(
    val pairNumber: Int,
    val start: String,
    val end: String,
)

object ScheduleEngine {

    val standardBells = listOf(
        BellSlot(1, "08:30", "10:00"),
        BellSlot(2, "10:15", "11:45"),
        BellSlot(3, "12:00", "13:30"),
        BellSlot(4, "14:00", "15:30"),
        BellSlot(5, "15:45", "17:15"),
        BellSlot(6, "17:30", "19:00"),
        BellSlot(7, "19:10", "20:40"),
    )

    fun mondayOf(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    /**
     * Вычисляет, является ли целевая неделя (заданная своим понедельником) верхней,
     * опираясь на известную опорную дату и её чётность от API WeekType/Get.
     */
    fun isWeekUpperForMonday(
        anchorDate: LocalDate?,
        anchorIsUpper: Boolean?,
        targetMonday: LocalDate,
    ): Boolean? {
        if (anchorDate == null || anchorIsUpper == null) return null
        val anchorMonday = mondayOf(anchorDate)
        val weeksDiff = ChronoUnit.WEEKS.between(anchorMonday, targetMonday)
        return if (Math.floorMod(weeksDiff, 2L) == 0L) {
            anchorIsUpper
        } else {
            !anchorIsUpper
        }
    }

    /**
     * Фильтрует пары для конкретного дня недели/даты и выбранной чётности недели.
     */
    fun lessonsForDay(
        allLessons: List<Lesson>,
        targetDate: LocalDate,
        weekFilter: WeekFilterMode,
    ): List<Lesson> {
        val targetDay = targetDate.dayOfWeek
        val dayCandidates = allLessons.filter { lesson ->
            if (lesson.dayOfWeek != targetDay) return@filter false

            if (lesson.explicitDate != null) {
                lesson.explicitDate == targetDate
            } else {
                when (lesson.parity) {
                    WeekParity.EVERY -> true
                    WeekParity.UPPER -> weekFilter == WeekFilterMode.UPPER
                    WeekParity.LOWER -> weekFilter == WeekFilterMode.LOWER
                }
            }
        }

        // Если на конкретную пару есть замена с явной датой (isChange && explicitDate == targetDate),
        // она остаётся вверху внутри своего слота и не теряется.
        return dayCandidates.sortedWith(
            compareBy<Lesson>(
                { if (it.pairNumber > 0) it.pairNumber else Int.MAX_VALUE },
                { it.startTime ?: LocalTime.MAX },
                { !it.isChange },
                { it.subject },
            )
        )
    }

    fun computeLiveLessonState(
        lessonsToday: List<Lesson>,
        nowTime: LocalTime,
    ): LiveLessonState {
        val timedLessons = lessonsToday
            .filter { it.startTime != null && it.endTime != null }
            .sortedBy { it.startTime }

        if (timedLessons.isEmpty()) return LiveLessonState.NoLessonsToday

        val current = timedLessons.firstOrNull { lesson ->
            val start = lesson.startTime!!
            val end = lesson.endTime!!
            !nowTime.isBefore(start) && nowTime.isBefore(end)
        }
        if (current != null) {
            val start = current.startTime!!
            val end = current.endTime!!
            val totalSeconds = Duration.between(start, end).seconds.coerceAtLeast(1L)
            val elapsedSeconds = Duration.between(start, nowTime).seconds.coerceIn(0L, totalSeconds)
            val remainingMinutes = ((totalSeconds - elapsedSeconds + 59L) / 60L).coerceAtLeast(1L)
            return LiveLessonState.InProgress(
                currentLesson = current,
                minutesRemaining = remainingMinutes,
                progress = elapsedSeconds.toFloat() / totalSeconds.toFloat(),
            )
        }

        val firstLesson = timedLessons.first()
        if (nowTime.isBefore(firstLesson.startTime!!)) {
            val minutes = Duration.between(nowTime, firstLesson.startTime).toMinutes().coerceAtLeast(1L)
            return LiveLessonState.BeforeFirstLesson(
                nextLesson = firstLesson,
                minutesUntilStart = minutes,
            )
        }

        val nextLesson = timedLessons.firstOrNull { it.startTime!!.isAfter(nowTime) }
        if (nextLesson != null) {
            val minutes = Duration.between(nowTime, nextLesson.startTime!!).toMinutes().coerceAtLeast(1L)
            return LiveLessonState.OnBreak(
                nextLesson = nextLesson,
                minutesUntilStart = minutes,
            )
        }

        return LiveLessonState.FinishedForToday
    }
}
