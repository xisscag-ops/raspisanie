package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.LessonType
import ru.ngieu.raspisanie.data.WeekParity
import ru.ngieu.raspisanie.domain.WeekFilterMode
import ru.ngieu.raspisanie.ui.share.ScheduleShareBuilder
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ScheduleShareBuilderTest {

    @Test
    fun `builds full Monday-to-Saturday week snapshot with grouped subgroups`() {
        val monday = LocalDate.of(2026, 11, 23)
        val lessons = listOf(
            lesson("m1a", DayOfWeek.MONDAY, 1, "(пр. 1) Иностранный язык", WeekParity.UPPER),
            lesson("m1b", DayOfWeek.MONDAY, 1, "(пр. 2) Иностранный язык", WeekParity.UPPER),
            lesson("m2", DayOfWeek.MONDAY, 2, "Математика", WeekParity.EVERY),
            lesson("t1", DayOfWeek.TUESDAY, 1, "Физика", WeekParity.LOWER), // skipped for UPPER
            lesson("w1", DayOfWeek.WEDNESDAY, 1, "Программирование", WeekParity.EVERY),
        )

        val snapshot = ScheduleShareBuilder.buildWeekSnapshot(
            groupName = "С-25ИО",
            selectedMonday = monday,
            weekFilter = WeekFilterMode.UPPER,
            allLessons = lessons,
        )

        assertEquals(6, snapshot.days.size)
        assertEquals(monday, snapshot.days[0].date)
        assertEquals(monday.plusDays(5), snapshot.days[5].date)

        // Monday: 2 slots (1st slot combines 2 subgroups, 2nd slot is Math)
        assertEquals(2, snapshot.days[0].slots.size)
        assertEquals(2, snapshot.days[0].slots[0].lessons.size)

        // Tuesday: 0 slots for UPPER week
        assertEquals(0, snapshot.days[1].slots.size)

        // Wednesday: 1 slot
        assertEquals(1, snapshot.days[2].slots.size)

        // Total slots across week = 3
        assertEquals(3, snapshot.totalPairsCount)
    }

    @Test
    fun `formats Russian pairs pluralization properly`() {
        assertEquals("Занятий нет", ScheduleShareBuilder.formatPairsCount(0))
        assertEquals("1 пара", ScheduleShareBuilder.formatPairsCount(1))
        assertEquals("2 пары", ScheduleShareBuilder.formatPairsCount(2))
        assertEquals("4 пары", ScheduleShareBuilder.formatPairsCount(4))
        assertEquals("5 пар", ScheduleShareBuilder.formatPairsCount(5))
        assertEquals("11 пар", ScheduleShareBuilder.formatPairsCount(11))
        assertEquals("21 пара", ScheduleShareBuilder.formatPairsCount(21))
    }

    private fun lesson(
        id: String,
        day: DayOfWeek,
        pair: Int,
        subject: String,
        parity: WeekParity,
    ) = Lesson(
        id = id,
        dayOfWeek = day,
        pairNumber = pair,
        timeRangeText = "08:00 – 09:30",
        startTime = LocalTime.of(8, 0),
        endTime = LocalTime.of(9, 30),
        subject = subject,
        teachers = listOf("Иванов И. И."),
        room = "101",
        note = "Практика",
        lessonType = LessonType.PRACTICE,
        parity = parity,
        explicitDate = null,
        isChange = false,
    )
}
