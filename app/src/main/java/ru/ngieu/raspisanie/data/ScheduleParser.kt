package ru.ngieu.raspisanie.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object ScheduleParser {

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val dateFormatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("yyyy.MM.dd"),
    )

    private val timeRegex = Regex("""(\d{1,2})[:.\-](\d{2})""")
    private val numberRegex = Regex("""(\d+)""")

    fun parseActors(rawJson: String): List<ActorDto> {
        val root = json.parseToJsonElement(rawJson)
        val array = extractArray(root, listOf("data", "actors", "result", "items")) ?: return emptyList()
        return array.mapNotNull { element ->
            runCatching { json.decodeFromJsonElement<ActorDto>(element) }.getOrNull()
        }.filter { it.id.isNotBlank() && it.name.isNotBlank() }
    }

    fun parseTypedActors(rawJson: String, kind: ActorKind): List<ScheduleActor> {
        return parseActors(rawJson)
            .distinctBy { it.id }
            .map { dto ->
                ScheduleActor(
                    id = dto.id.trim(),
                    name = dto.name.trim(),
                    kind = kind,
                    departmentId = dto.departmentId,
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    fun searchActors(
        actors: List<ScheduleActor>,
        query: String,
        filter: ActorKind? = null,
    ): List<ScheduleActor> {
        val scoped = if (filter != null) {
            actors.filter { it.kind == filter }
        } else {
            actors
        }
        val trimmed = query.trim().lowercase()
        if (trimmed.isEmpty()) {
            return scoped.sortedWith(compareBy({ it.kind.ordinal }, { it.name.lowercase() }))
        }
        val normalizedCompact = trimmed.replace("-", "").replace(" ", "")
        return scoped
            .mapNotNull { actor ->
                val lowerName = actor.name.lowercase()
                val compactName = lowerName.replace("-", "").replace(" ", "")
                val rank = when {
                    lowerName.startsWith(trimmed) || compactName.startsWith(normalizedCompact) -> 0
                    lowerName.split(" ", "-").any { it.startsWith(trimmed) } -> 1
                    lowerName.contains(trimmed) || compactName.contains(normalizedCompact) -> 2
                    else -> return@mapNotNull null
                }
                rank to actor
            }
            .sortedWith(compareBy({ it.first }, { it.second.kind.ordinal }, { it.second.name.lowercase() }))
            .map { it.second }
    }

    fun findGroupActorId(actors: List<ActorDto>, targetGroupName: String): String? {
        val normalizedTarget = normalizeGroupName(targetGroupName)
        return actors.firstOrNull { normalizeGroupName(it.name) == normalizedTarget }?.id
    }

    fun parseWeekType(rawJson: String): WeekTypeInfo? {
        val root = json.parseToJsonElement(rawJson)
        val obj: JsonObject? = when (root) {
            is JsonArray -> root.firstOrNull() as? JsonObject
            is JsonObject -> {
                val nested = extractArray(root, listOf("data", "result"))
                if (nested != null) {
                    nested.firstOrNull() as? JsonObject
                } else {
                    root
                }
            }
            else -> null
        }
        if (obj == null) return null

        val isUpper = findFieldCaseInsensitive(obj, "IsUpperWeek")?.let { parseBooleanElement(it) }
            ?: return null

        val dateStr = findFieldCaseInsensitive(obj, "Date")
            ?.let { (it as? JsonPrimitive)?.contentOrNull }
        val parsedDate = dateStr?.let(::parseDate) ?: LocalDate.now()

        return WeekTypeInfo(date = parsedDate, isUpperWeek = isUpper)
    }

    fun parseSchedule(rawJson: String): List<Lesson> {
        val root = json.parseToJsonElement(rawJson)
        val array = extractArray(root, listOf("data", "schedule", "result", "items")) ?: return emptyList()

        return array.flatMapIndexed { index, element ->
            val dto = runCatching { json.decodeFromJsonElement<RawScheduleItem>(element) }.getOrNull()
                ?: return@flatMapIndexed emptyList()
            mapDtoToLessons(dto, index)
        }
    }

    private fun mapDtoToLessons(dto: RawScheduleItem, index: Int): List<Lesson> {
        val day = parseDayOfWeek(dto.dayName) ?: return emptyList()
        val rawSubjects = dto.subjects.map { it.trim() }
        val hasMultipleSubgroups = rawSubjects.count { it.isNotEmpty() } > 1

        if (hasMultipleSubgroups) {
            return rawSubjects.mapIndexedNotNull { subIndex, subTitle ->
                if (subTitle.isEmpty()) return@mapIndexedNotNull null
                val teacher = dto.instructors.getOrNull(subIndex)?.trim()?.takeIf { it.isNotEmpty() }
                    ?: dto.instructors.singleOrNull()?.trim()?.takeIf { it.isNotEmpty() }
                val room = dto.offices.getOrNull(subIndex)?.trim()?.takeIf { it.isNotEmpty() }
                    ?: dto.offices.singleOrNull()?.trim().orEmpty()
                val note = dto.notes.getOrNull(subIndex)?.trim()?.takeIf { it.isNotEmpty() }
                    ?: dto.notes.singleOrNull()?.trim().orEmpty()
                val group = dto.groups.getOrNull(subIndex)?.trim()?.takeIf { it.isNotEmpty() }
                    ?: dto.groups.singleOrNull()?.trim()?.takeIf { it.isNotEmpty() }
                buildLesson(
                    dto = dto,
                    day = day,
                    subject = subTitle,
                    teachers = listOfNotNull(teacher),
                    groups = listOfNotNull(group),
                    room = room,
                    note = note,
                    uniqueSuffix = "${index}_$subIndex",
                )
            }
        }

        val subject = rawSubjects.firstOrNull { it.isNotEmpty() }.orEmpty()
        val teachers = dto.instructors.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val groups = dto.groups.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val room = dto.offices.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(", ")
        val note = dto.notes.map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(" • ")

        if (subject.isEmpty() && note.isEmpty()) return emptyList()

        return listOfNotNull(
            buildLesson(
                dto = dto,
                day = day,
                subject = subject.ifEmpty { note },
                teachers = teachers,
                groups = groups,
                room = room,
                note = note,
                uniqueSuffix = index.toString(),
            )
        )
    }

    private fun buildLesson(
        dto: RawScheduleItem,
        day: DayOfWeek,
        subject: String,
        teachers: List<String>,
        groups: List<String>,
        room: String,
        note: String,
        uniqueSuffix: String,
    ): Lesson {
        val pairNumber = parsePairNumber(dto.classNumberName)
        val (startTime, endTime) = parseClassTime(dto.classTime)
        val formattedTime = when {
            startTime != null && endTime != null -> "${formatTime(startTime)} – ${formatTime(endTime)}"
            dto.classTime.isNotBlank() -> dto.classTime.trim()
            else -> "Время не указано"
        }

        val explicitDate = dto.date?.let(::parseDate)
        val parity = when (dto.isUpperWeek) {
            true -> WeekParity.UPPER
            false -> WeekParity.LOWER
            null -> WeekParity.EVERY
        }

        val lessonType = detectLessonType(note = note, subject = subject)
        val id = "${day.value}|$pairNumber|$subject|$room|${dto.isUpperWeek}|${dto.isChange}|$explicitDate|$uniqueSuffix"

        return Lesson(
            id = id,
            dayOfWeek = day,
            pairNumber = pairNumber,
            timeRangeText = formattedTime,
            startTime = startTime,
            endTime = endTime,
            subject = subject,
            teachers = teachers,
            room = room,
            note = note,
            lessonType = lessonType,
            parity = parity,
            explicitDate = explicitDate,
            isChange = dto.isChange,
            groups = groups,
        )
    }

    fun parseDayOfWeek(dayName: String): DayOfWeek? {
        return when (dayName.trim().lowercase()) {
            "понедельник", "пн" -> DayOfWeek.MONDAY
            "вторник", "вт" -> DayOfWeek.TUESDAY
            "среда", "ср" -> DayOfWeek.WEDNESDAY
            "четверг", "чт" -> DayOfWeek.THURSDAY
            "пятница", "пт" -> DayOfWeek.FRIDAY
            "суббота", "сб" -> DayOfWeek.SATURDAY
            "воскресенье", "вс" -> DayOfWeek.SUNDAY
            else -> null
        }
    }

    fun parsePairNumber(raw: String): Int {
        val match = numberRegex.find(raw) ?: return 0
        return match.groupValues[1].toIntOrNull() ?: 0
    }

    fun parseClassTime(raw: String): Pair<LocalTime?, LocalTime?> {
        val matches = timeRegex.findAll(raw).toList()
        if (matches.size < 2) return null to null
        val start = toLocalTime(matches[0])
        val end = toLocalTime(matches[1])
        return start to end
    }

    private fun toLocalTime(match: MatchResult): LocalTime? {
        val h = match.groupValues[1].toIntOrNull() ?: return null
        val m = match.groupValues[2].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return LocalTime.of(h, m)
    }

    fun parseDate(raw: String): LocalDate? {
        val cleaned = raw.trim()
        if (cleaned.isEmpty()) return null

        // Handle ISO timestamps like "2026-11-22T00:00:00" or "2026-11-22T00:00:00Z"
        val datePart = if (cleaned.length >= 10 && cleaned[4] == '-' && cleaned[7] == '-') {
            cleaned.substring(0, 10)
        } else {
            cleaned.substringBefore(' ')
        }

        for (formatter in dateFormatters) {
            try {
                return LocalDate.parse(datePart, formatter)
            } catch (_: DateTimeParseException) {
            }
        }
        return null
    }

    fun detectLessonType(note: String, subject: String): LessonType {
        val combined = "$note $subject".lowercase()
        return when {
            "мероприят" in combined -> LessonType.EVENT
            "экз" in combined -> LessonType.EXAM
            "конс" in combined || "зач" in combined -> LessonType.CONSULTATION
            "лаб" in combined -> LessonType.LAB
            "практ" in combined || "семин" in combined -> LessonType.PRACTICE
            "лекц" in combined -> LessonType.LECTURE
            else -> LessonType.OTHER
        }
    }

    private fun formatTime(time: LocalTime): String {
        return "%02d:%02d".format(time.hour, time.minute)
    }

    fun normalizeGroupName(name: String): String {
        return name.trim().replace(" ", "").uppercase()
    }

    private fun extractArray(root: JsonElement, keys: List<String>): JsonArray? {
        return when (root) {
            is JsonArray -> root
            is JsonObject -> keys.firstNotNullOfOrNull { key -> root[key] as? JsonArray }
            else -> null
        }
    }

    private fun findFieldCaseInsensitive(obj: JsonObject, targetKey: String): JsonElement? {
        return obj.entries.firstOrNull { it.key.equals(targetKey, ignoreCase = true) }?.value
    }

    private fun parseBooleanElement(element: JsonElement): Boolean? {
        val primitive = element as? JsonPrimitive ?: return null
        primitive.booleanOrNull?.let { return it }
        return when (primitive.contentOrNull?.trim()?.lowercase()) {
            "true", "1", "yes", "да" -> true
            "false", "0", "no", "нет" -> false
            else -> null
        }
    }
}
