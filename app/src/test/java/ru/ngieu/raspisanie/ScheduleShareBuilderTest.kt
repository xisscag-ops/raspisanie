package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.ngieu.raspisanie.data.ActorKind
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
    fun `builds single-day snapshot with grouped subgroups and filters other days`() {
        val monday = LocalDate.of(2026, 11, 23)
        val tuesday = LocalDate.of(2026, 11, 24)
        val lessons = listOf(
            lesson("m1a", DayOfWeek.MONDAY, 1, "(пр. 1) Иностранный язык", WeekParity.UPPER),
            lesson("m1b", DayOfWeek.MONDAY, 1, "(пр. 2) Иностранный язык", WeekParity.UPPER),
            lesson("m2", DayOfWeek.MONDAY, 2, "Математика", WeekParity.EVERY),
            lesson("t1", DayOfWeek.TUESDAY, 1, "Физика", WeekParity.UPPER),
        )

        val mondaySnapshot = ScheduleShareBuilder.buildDaySnapshot(
            actorName = "С-25ИО",
            actorKind = ActorKind.GROUP,
            selectedDate = monday,
            weekFilter = WeekFilterMode.UPPER,
            allLessons = lessons,
        )

        assertEquals("С-25ИО", mondaySnapshot.actorBadgeName)
        assertEquals("Группа С-25ИО", mondaySnapshot.actorTitle)
        assertEquals(ActorKind.GROUP, mondaySnapshot.actorKind)
        assertEquals(monday, mondaySnapshot.date)
        assertEquals(2, mondaySnapshot.slots.size)
        assertEquals(2, mondaySnapshot.slots[0].lessons.size)
        assertEquals(2, mondaySnapshot.pairsCount)

        val tuesdaySnapshot = ScheduleShareBuilder.buildDaySnapshot(
            actorName = "Иванов И. И.",
            actorKind = ActorKind.TEACHER,
            selectedDate = tuesday,
            weekFilter = WeekFilterMode.UPPER,
            allLessons = lessons,
        )

        assertEquals(ActorKind.TEACHER, tuesdaySnapshot.actorKind)
        assertEquals("Иванов И. И.", tuesdaySnapshot.actorTitle)
        assertEquals(1, tuesdaySnapshot.slots.size)
        assertEquals("Физика", tuesdaySnapshot.slots[0].lessons[0].subject)
        assertEquals(listOf("С-25ИО"), tuesdaySnapshot.slots[0].lessons[0].groups)
    }

    @Test
    fun `returns empty slots when day has no matching lessons for week parity`() {
        val tuesday = LocalDate.of(2026, 11, 24)
        val lessons = listOf(
            lesson("t1", DayOfWeek.TUESDAY, 1, "Физика", WeekParity.LOWER),
        )

        val snapshot = ScheduleShareBuilder.buildDaySnapshot(
            actorName = "С-25ИО",
            actorKind = ActorKind.GROUP,
            selectedDate = tuesday,
            weekFilter = WeekFilterMode.UPPER,
            allLessons = lessons,
        )

        assertEquals(0, snapshot.slots.size)
        assertEquals(0, snapshot.pairsCount)
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
        groups = listOf("С-25ИО"),
        room = "101",
        note = "Практика",
        lessonType = LessonType.PRACTICE,
        parity = parity,
        explicitDate = null,
        isChange = false,
    )
}
