package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.ngieu.raspisanie.data.LessonType
import ru.ngieu.raspisanie.data.ScheduleParser
import ru.ngieu.raspisanie.data.WeekParity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ScheduleParserTest {

    @Test
    fun `finds C-25IO actor id regardless of whitespace`() {
        val raw = """
            [
              {"id":"id-other","departmentId":1,"name":"Б-25ИО"},
              {"id":"a11ce001-0000-0001-0000-00a200000000","departmentId":2,"name":"С-25ИО"}
            ]
        """.trimIndent()

        val actors = ScheduleParser.parseActors(raw)
        assertEquals(2, actors.size)
        assertEquals(
            "a11ce001-0000-0001-0000-00a200000000",
            ScheduleParser.findGroupActorId(actors, "С-25ИО"),
        )
    }

    @Test
    fun `parses WeekType array with PascalCase fields`() {
        val raw = """[{"Date":"2026-11-23T00:00:00","IsUpperWeek":true}]"""
        val weekInfo = ScheduleParser.parseWeekType(raw)

        assertNotNull(weekInfo)
        assertEquals(LocalDate.of(2026, 11, 23), weekInfo?.date)
        assertTrue(weekInfo?.isUpperWeek == true)
    }

    @Test
    fun `parses WeekType object with camelCase fields`() {
        val raw = """{"date":"23.11.2026","isUpperWeek":false}"""
        val weekInfo = ScheduleParser.parseWeekType(raw)

        assertNotNull(weekInfo)
        assertEquals(LocalDate.of(2026, 11, 23), weekInfo?.date)
        assertFalse(weekInfo?.isUpperWeek ?: true)
    }

    @Test
    fun `parses schedule items including changes and time ranges`() {
        val raw = """
            [
              {
                "dayName": "Понедельник",
                "classNumberName": "1 пара",
                "classTime": "08:30 / 10:00",
                "groups": ["С-25ИО"],
                "subjects": ["Базы данных"],
                "instructors": ["Иванов И.И."],
                "offices": ["205"],
                "notes": ["Лекция"],
                "date": null,
                "isUpperWeek": true,
                "isChange": false
              },
              {
                "dayName": "Вторник",
                "classNumberName": "2 пара",
                "classTime": "10:15-11:45",
                "groups": ["С-25ИО"],
                "subjects": ["Операционные системы"],
                "instructors": ["Петров П.П."],
                "offices": ["312"],
                "notes": ["Лабораторная работа"],
                "date": "2026-11-24",
                "isUpperWeek": null,
                "isChange": true
              }
            ]
        """.trimIndent()

        val lessons = ScheduleParser.parseSchedule(raw)
        assertEquals(2, lessons.size)

        val first = lessons[0]
        assertEquals(DayOfWeek.MONDAY, first.dayOfWeek)
        assertEquals(1, first.pairNumber)
        assertEquals(LocalTime.of(8, 30), first.startTime)
        assertEquals(LocalTime.of(10, 0), first.endTime)
        assertEquals(LessonType.LECTURE, first.lessonType)
        assertEquals(WeekParity.UPPER, first.parity)
        assertNull(first.explicitDate)
        assertFalse(first.isChange)

        val second = lessons[1]
        assertEquals(DayOfWeek.TUESDAY, second.dayOfWeek)
        assertEquals(2, second.pairNumber)
        assertEquals(LessonType.LAB, second.lessonType)
        assertEquals(WeekParity.EVERY, second.parity)
        assertEquals(LocalDate.of(2026, 11, 24), second.explicitDate)
        assertTrue(second.isChange)
    }

    @Test
    fun `parses hyphenated times and splits multiple subgroups`() {
        val raw = """
            [
              {
                "dayName": "Вторник",
                "classNumberName": "1 пара",
                "classTime": "08-00 / 09-30",
                "groups": ["С-25ИО", "С-25ИО"],
                "subjects": [
                  "(пр. 2) Иностранный язык",
                  "(пр. 1) Иностранный язык"
                ],
                "instructors": ["Митина И. В.", "Демидова Е. Е."],
                "offices": ["112", "113"],
                "notes": ["Практические занятия", "Практические занятия"],
                "isUpperWeek": true,
                "date": null,
                "isChange": false
              }
            ]
        """.trimIndent()

        val lessons = ScheduleParser.parseSchedule(raw)
        assertEquals(2, lessons.size)
        assertEquals(LocalTime.of(8, 0), lessons[0].startTime)
        assertEquals(LocalTime.of(9, 30), lessons[0].endTime)
        assertEquals("08:00 – 09:30", lessons[0].timeRangeText)
        assertEquals("112", lessons[0].room)
        assertEquals(listOf("Митина И. В."), lessons[0].teachers)
        assertEquals("113", lessons[1].room)
        assertEquals(listOf("Демидова Е. Е."), lessons[1].teachers)
    }
}
