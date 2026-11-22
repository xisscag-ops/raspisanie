package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.LessonType
import ru.ngieu.raspisanie.data.WeekParity
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.domain.WeekFilterMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ScheduleEngineTest {

    @Test
    fun `alternates week parity across consecutive weeks`() {
        val anchorDate = LocalDate.of(2026, 11, 23) // Monday
        val currentMonday = ScheduleEngine.mondayOf(anchorDate)
        val nextMonday = currentMonday.plusWeeks(1)
        val twoWeeksLaterMonday = currentMonday.plusWeeks(2)

        assertEquals(true, ScheduleEngine.isWeekUpperForMonday(anchorDate, true, currentMonday))
        assertEquals(false, ScheduleEngine.isWeekUpperForMonday(anchorDate, true, nextMonday))
        assertEquals(true, ScheduleEngine.isWeekUpperForMonday(anchorDate, true, twoWeeksLaterMonday))
    }

    @Test
    fun `filters day lessons by parity and explicit date`() {
        val targetMonday = LocalDate.of(2026, 11, 23)
        val otherMonday = LocalDate.of(2026, 11, 30)

        val upperLesson = sampleLesson(
            id = "1",
            pair = 1,
            parity = WeekParity.UPPER,
            explicitDate = null,
        )
        val lowerLesson = sampleLesson(
            id = "2",
            pair = 2,
            parity = WeekParity.LOWER,
            explicitDate = null,
        )
        val everyWeekLesson = sampleLesson(
            id = "3",
            pair = 3,
            parity = WeekParity.EVERY,
            explicitDate = null,
        )
        val datedChange = sampleLesson(
            id = "4",
            pair = 1,
            parity = WeekParity.EVERY,
            explicitDate = targetMonday,
            isChange = true,
        )

        val all = listOf(upperLesson, lowerLesson, everyWeekLesson, datedChange)

        val upperResult = ScheduleEngine.lessonsForDay(all, targetMonday, WeekFilterMode.UPPER)
        assertEquals(listOf("4", "1", "3"), upperResult.map { it.id })

        val lowerNextWeek = ScheduleEngine.lessonsForDay(all, otherMonday, WeekFilterMode.LOWER)
        assertEquals(listOf("2", "3"), lowerNextWeek.map { it.id })
    }

    @Test
    fun `computes live lesson states accurately`() {
        val lesson1 = sampleLesson(
            id = "1",
            pair = 1,
            start = LocalTime.of(8, 30),
            end = LocalTime.of(10, 0),
        )
        val lesson2 = sampleLesson(
            id = "2",
            pair = 2,
            start = LocalTime.of(10, 15),
            end = LocalTime.of(11, 45),
        )
        val today = listOf(lesson1, lesson2)

        val before = ScheduleEngine.computeLiveLessonState(today, LocalTime.of(8, 0))
        assertTrue(before is LiveLessonState.BeforeFirstLesson)
        assertEquals(30L, (before as LiveLessonState.BeforeFirstLesson).minutesUntilStart)

        val during = ScheduleEngine.computeLiveLessonState(today, LocalTime.of(9, 15))
        assertTrue(during is LiveLessonState.InProgress)
        assertEquals(45L, (during as LiveLessonState.InProgress).minutesRemaining)

        val onBreak = ScheduleEngine.computeLiveLessonState(today, LocalTime.of(10, 5))
        assertTrue(onBreak is LiveLessonState.OnBreak)
        assertEquals(10L, (onBreak as LiveLessonState.OnBreak).minutesUntilStart)

        val finished = ScheduleEngine.computeLiveLessonState(today, LocalTime.of(12, 0))
        assertTrue(finished is LiveLessonState.FinishedForToday)
        assertFalse(finished is LiveLessonState.NoLessonsToday)
    }

    private fun sampleLesson(
        id: String,
        pair: Int,
        parity: WeekParity = WeekParity.EVERY,
        explicitDate: LocalDate? = null,
        isChange: Boolean = false,
        start: LocalTime = LocalTime.of(8, 30),
        end: LocalTime = LocalTime.of(10, 0),
    ) = Lesson(
        id = id,
        dayOfWeek = DayOfWeek.MONDAY,
        pairNumber = pair,
        timeRangeText = "$start – $end",
        startTime = start,
        endTime = end,
        subject = "Предмет $id",
        teachers = listOf("Преподаватель"),
        room = "101",
        note = "",
        lessonType = LessonType.LECTURE,
        parity = parity,
        explicitDate = explicitDate,
        isChange = isChange,
    )
}
