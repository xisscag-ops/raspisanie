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

    const val MAX_WEEK_OFFSET = 52
    const val DAYS_PER_STUDY_WEEK = 6
    const val TOTAL_WEEK_PAGES = MAX_WEEK_OFFSET * 2 + 1
    const val TOTAL_DAY_PAGES = TOTAL_WEEK_PAGES * DAYS_PER_STUDY_WEEK

    val standardBells = listOf(
        BellSlot(1, "08:00", "09:30"),
        BellSlot(2, "09:40", "11:10"),
        BellSlot(3, "11:20 / 12:05", "12:50 / 13:35"),
        BellSlot(4, "13:45", "15:15"),
        BellSlot(5, "15:25", "16:55"),
        BellSlot(6, "17:05", "18:35"),
        BellSlot(7, "18:45", "20:15"),
    )

    fun mondayOf(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    fun dayIndexToDate(anchorMonday: LocalDate, pageIndex: Int): LocalDate {
        val clamped = pageIndex.coerceIn(0, TOTAL_DAY_PAGES - 1)
        val weekOffset = clamped / DAYS_PER_STUDY_WEEK - MAX_WEEK_OFFSET
        val dayOffset = clamped % DAYS_PER_STUDY_WEEK
        return anchorMonday.plusWeeks(weekOffset.toLong()).plusDays(dayOffset.toLong())
    }

    fun dateToDayIndex(anchorMonday: LocalDate, date: LocalDate): Int {
        val monday = mondayOf(date)
        val weekDiff = ChronoUnit.WEEKS.between(anchorMonday, monday)
            .toInt()
            .coerceIn(-MAX_WEEK_OFFSET, MAX_WEEK_OFFSET)
        val dayOffset = (date.dayOfWeek.value - 1).coerceIn(0, DAYS_PER_STUDY_WEEK - 1)
        return (weekDiff + MAX_WEEK_OFFSET) * DAYS_PER_STUDY_WEEK + dayOffset
    }

    fun weekIndexToMonday(anchorMonday: LocalDate, pageIndex: Int): LocalDate {
        val clamped = pageIndex.coerceIn(0, TOTAL_WEEK_PAGES - 1)
        return anchorMonday.plusWeeks((clamped - MAX_WEEK_OFFSET).toLong())
    }

    fun mondayToWeekIndex(anchorMonday: LocalDate, monday: LocalDate): Int {
        val weekDiff = ChronoUnit.WEEKS.between(anchorMonday, monday)
            .toInt()
            .coerceIn(-MAX_WEEK_OFFSET, MAX_WEEK_OFFSET)
        return weekDiff + MAX_WEEK_OFFSET
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

        // Если на конкретную дату и пару есть общегрупповая замена (isChange && explicitDate == targetDate),
        // она заменяет обычные пары в этом слоте; если замена точечная для подгруппы — оставляем оба варианта.
        val wholeGroupReplacedPairs = dayCandidates
            .filter {
                it.isChange &&
                    it.explicitDate == targetDate &&
                    it.pairNumber > 0 &&
                    !it.subject.contains("(пр.", ignoreCase = true)
            }
            .map { it.pairNumber }
            .toSet()

        val resolved = dayCandidates.filter { lesson ->
            if (!lesson.isChange && lesson.pairNumber in wholeGroupReplacedPairs) {
                false
            } else {
                true
            }
        }

        return resolved.sortedWith(
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
