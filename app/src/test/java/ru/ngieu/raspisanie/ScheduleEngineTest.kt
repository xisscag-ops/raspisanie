package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.LessonType
import ru.ngieu.raspisanie.data.SubgroupFilterMode
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
        assertEquals(listOf("4", "3"), upperResult.map { it.id })

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

    @Test
    fun `maps day and week pager indices symmetrically across study weeks`() {
        val anchorMonday = LocalDate.of(2026, 11, 23) // Monday

        // Monday of anchor week
        val anchorIndex = ScheduleEngine.dateToDayIndex(anchorMonday, anchorMonday)
        assertEquals(ScheduleEngine.MAX_WEEK_OFFSET * ScheduleEngine.DAYS_PER_STUDY_WEEK, anchorIndex)
        assertEquals(anchorMonday, ScheduleEngine.dayIndexToDate(anchorMonday, anchorIndex))

        // Saturday of anchor week -> next page is Monday of next week (skips Sunday)
        val saturday = anchorMonday.plusDays(5)
        val saturdayIndex = ScheduleEngine.dateToDayIndex(anchorMonday, saturday)
        assertEquals(anchorIndex + 5, saturdayIndex)
        assertEquals(saturday, ScheduleEngine.dayIndexToDate(anchorMonday, saturdayIndex))
        assertEquals(anchorMonday.plusWeeks(1), ScheduleEngine.dayIndexToDate(anchorMonday, saturdayIndex + 1))

        // Previous week Friday
        val prevFriday = anchorMonday.minusDays(3)
        val prevFridayIndex = ScheduleEngine.dateToDayIndex(anchorMonday, prevFriday)
        assertEquals(prevFriday, ScheduleEngine.dayIndexToDate(anchorMonday, prevFridayIndex))

        // Week index round-trips
        val weekIdx = ScheduleEngine.mondayToWeekIndex(anchorMonday, anchorMonday.plusWeeks(3))
        assertEquals(ScheduleEngine.MAX_WEEK_OFFSET + 3, weekIdx)
        assertEquals(anchorMonday.plusWeeks(3), ScheduleEngine.weekIndexToMonday(anchorMonday, weekIdx))
    }

    @Test
    fun `groups subgroup lessons in the same pair slot into one slot ordered by subgroup`() {
        val sub2 = sampleLesson(
            id = "sub2",
            pair = 1,
            start = LocalTime.of(8, 0),
            end = LocalTime.of(9, 30),
        ).copy(subject = "(пр. 2) Иностранный язык")
        val sub1 = sampleLesson(
            id = "sub1",
            pair = 1,
            start = LocalTime.of(8, 0),
            end = LocalTime.of(9, 30),
        ).copy(subject = "(пр. 1) Иностранный язык")
        val pair2 = sampleLesson(
            id = "p2",
            pair = 2,
            start = LocalTime.of(9, 40),
            end = LocalTime.of(11, 10),
        ).copy(subject = "Математика")

        val slots = ScheduleEngine.groupLessonsIntoSlots(listOf(sub2, sub1, pair2))
        assertEquals(2, slots.size)
        assertEquals(1, slots[0].pairNumber)
        assertEquals(listOf("sub1", "sub2"), slots[0].lessons.map { it.id })
        assertEquals(2, slots[1].pairNumber)
        assertEquals(listOf("p2"), slots[1].lessons.map { it.id })
    }

    @Test
    fun `extracts subgroup number and strips redundant subgroup prefix`() {
        assertEquals(1, ScheduleEngine.extractSubgroupNumber(sampleLesson("s1", 1).copy(subject = "(пр. 1) Иностранный язык")))
        assertEquals(2, ScheduleEngine.extractSubgroupNumber(sampleLesson("s2", 1).copy(subject = "(2 п/г) Базы данных")))
        assertEquals("Иностранный язык", ScheduleEngine.cleanSubgroupPrefix("(пр. 1) Иностранный язык"))
        assertEquals("Базы данных", ScheduleEngine.cleanSubgroupPrefix("(2 п/г) Базы данных"))
        assertEquals("Математика", ScheduleEngine.cleanSubgroupPrefix("Математика"))
    }

    @Test
    fun `filters day lessons by selected subgroup while keeping common lessons`() {
        val targetMonday = LocalDate.of(2026, 11, 23)
        val common = sampleLesson("common", pair = 1).copy(subject = "Математика")
        val sub1 = sampleLesson("sub1", pair = 2).copy(subject = "(пр. 1) Программирование")
        val sub2 = sampleLesson("sub2", pair = 2).copy(subject = "(пр. 2) Иностранный язык")
        val all = listOf(common, sub1, sub2)

        assertEquals(
            listOf("common", "sub1", "sub2"),
            ScheduleEngine.lessonsForDay(all, targetMonday, WeekFilterMode.UPPER, SubgroupFilterMode.ALL).map { it.id },
        )
        assertEquals(
            listOf("common", "sub1"),
            ScheduleEngine.lessonsForDay(all, targetMonday, WeekFilterMode.UPPER, SubgroupFilterMode.FIRST).map { it.id },
        )
        assertEquals(
            listOf("common", "sub2"),
            ScheduleEngine.lessonsForDay(all, targetMonday, WeekFilterMode.UPPER, SubgroupFilterMode.SECOND).map { it.id },
        )
    }

    @Test
    fun `subgroup-only dated replacement hides unreplaced base lessons in that pair slot`() {
        val targetTuesday = LocalDate.of(2026, 11, 24)

        val basePair1 = sampleLesson("base1", pair = 1, parity = WeekParity.LOWER).copy(
            dayOfWeek = DayOfWeek.TUESDAY,
            subject = "Элементы высшей математики",
        )
        val basePair2 = sampleLesson("base2", pair = 2, parity = WeekParity.LOWER).copy(
            dayOfWeek = DayOfWeek.TUESDAY,
            subject = "История",
        )
        val basePair4 = sampleLesson("base4", pair = 4, parity = WeekParity.LOWER).copy(
            dayOfWeek = DayOfWeek.TUESDAY,
            subject = "Физика",
        )

        val changePair1Sub1 = sampleLesson(
            id = "chg1",
            pair = 1,
            explicitDate = targetTuesday,
            isChange = true,
        ).copy(dayOfWeek = DayOfWeek.TUESDAY, subject = "(пр. 1) Практика")
        val changePair2Sub1 = sampleLesson(
            id = "chg2",
            pair = 2,
            explicitDate = targetTuesday,
            isChange = true,
        ).copy(dayOfWeek = DayOfWeek.TUESDAY, subject = "(пр. 1) Практика")
        val changePair4All = sampleLesson(
            id = "chg4",
            pair = 4,
            explicitDate = targetTuesday,
            isChange = true,
        ).copy(dayOfWeek = DayOfWeek.TUESDAY, subject = "Практика")

        val all = listOf(basePair1, basePair2, basePair4, changePair1Sub1, changePair2Sub1, changePair4All)

        assertEquals(
            listOf("chg1", "chg2", "chg4"),
            ScheduleEngine.lessonsForDay(all, targetTuesday, WeekFilterMode.LOWER, SubgroupFilterMode.ALL).map { it.id },
        )
        assertEquals(
            listOf("chg1", "chg2", "chg4"),
            ScheduleEngine.lessonsForDay(all, targetTuesday, WeekFilterMode.LOWER, SubgroupFilterMode.FIRST).map { it.id },
        )
        assertEquals(
            listOf("chg4"),
            ScheduleEngine.lessonsForDay(all, targetTuesday, WeekFilterMode.LOWER, SubgroupFilterMode.SECOND).map { it.id },
        )
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
