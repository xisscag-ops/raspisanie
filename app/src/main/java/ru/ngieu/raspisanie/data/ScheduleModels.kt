package ru.ngieu.raspisanie.data

import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

@Serializable
enum class ActorKind {
    GROUP,
    TEACHER,
}

@Serializable
data class ScheduleActor(
    val id: String,
    val name: String,
    val kind: ActorKind,
    val departmentId: Int = 0,
)

@Serializable
data class CachedActorsBundle(
    val actors: List<ScheduleActor>,
    val updatedAtEpochMillis: Long,
)

@Serializable
data class ActorDto(
    val id: String = "",
    val departmentId: Int = 0,
    val name: String = "",
)

@Serializable
data class RawScheduleItem(
    val dayName: String = "",
    val classNumberName: String = "",
    val classTime: String = "",
    val groups: List<String> = emptyList(),
    val subjects: List<String> = emptyList(),
    val instructors: List<String> = emptyList(),
    val offices: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val date: String? = null,
    val isUpperWeek: Boolean? = null,
    val isChange: Boolean = false,
)

data class WeekTypeInfo(
    val date: LocalDate,
    val isUpperWeek: Boolean,
)

enum class LessonType(val label: String) {
    LECTURE("Лекция"),
    PRACTICE("Практика"),
    LAB("Лаб. работа"),
    EXAM("Экзамен"),
    CONSULTATION("Консультация"),
    EVENT("Мероприятие"),
    OTHER("Занятие"),
}

enum class WeekParity(val label: String) {
    UPPER("Верхняя"),
    LOWER("Нижняя"),
    EVERY("Каждую неделю"),
}

data class Lesson(
    val id: String,
    val dayOfWeek: DayOfWeek,
    val pairNumber: Int,
    val timeRangeText: String,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val subject: String,
    val teachers: List<String>,
    val room: String,
    val note: String,
    val lessonType: LessonType,
    val parity: WeekParity,
    val explicitDate: LocalDate?,
    val isChange: Boolean,
    val groups: List<String> = emptyList(),
)

@Serializable
data class CachedScheduleBundle(
    val actorId: String,
    val groupName: String,
    val rawScheduleJson: String,
    val weekAnchorDateIso: String? = null,
    val weekAnchorIsUpper: Boolean? = null,
    val updatedAtEpochMillis: Long,
)

data class ScheduleSnapshot(
    val groupName: String,
    val actorId: String,
    val lessons: List<Lesson>,
    val weekAnchorDate: LocalDate?,
    val weekAnchorIsUpper: Boolean?,
    val updatedAtEpochMillis: Long,
    val isFromCache: Boolean,
    val actorKind: ActorKind = ActorKind.GROUP,
)

enum class ThemeModeSetting(val label: String) {
    NEON("Неон"),
    SYSTEM("Система"),
    LIGHT("Светлая"),
    DARK("Тёмная"),
}
