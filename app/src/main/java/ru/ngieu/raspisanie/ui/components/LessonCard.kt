package ru.ngieu.raspisanie.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.ngieu.raspisanie.R
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors
import java.time.Duration
import java.time.LocalTime

@Composable
fun LessonCard(
    slot: LessonSlot,
    isCurrentNow: Boolean,
    isToday: Boolean,
    isPastDay: Boolean,
    nowTime: LocalTime,
    themeMode: ThemeModeSetting,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    showGroupsInsteadOfTeachers: Boolean = false,
) {
    val style = LocalAppVisualStyle.current
    val isDark = style.isDark
    val hasAnyChange = slot.lessons.any { it.isChange }
    val allChanges = slot.lessons.all { it.isChange }

    val activeNowAccent = when {
        style.isGlass -> GlassPalette.timelineActiveNow
        style.isNeon || isDark -> NeonPalette.timelineActiveNow
        else -> ScheduleAccentColors.currentLessonBorderLight
    }

    val timelineDotColor by animateColorAsState(
        targetValue = when {
            isCurrentNow -> activeNowAccent
            hasAnyChange -> if (isDark || style.isTranslucent) ScheduleAccentColors.changeBadgeDark else ScheduleAccentColors.changeBorderLight
            style.isNeon -> NeonPalette.timelinePurple
            style.isGlass -> GlassPalette.timelineDot
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(110),
        label = "timelineDot",
    )

    val lineColor = when {
        style.isNeon -> Color(0xFF7C3AED).copy(alpha = 0.72f)
        style.isGlass -> GlassPalette.timelineLine
        else -> Color(0xFF94A3B8).copy(alpha = 0.45f)
    }

    // Используем drawBehind на внешнем Row без IntrinsicSize.Min, чтобы FlowRow и подгруппы не обрезались
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val centerX = 9.dp.toPx()
                val centerY = size.height / 2f

                if (!isFirst) {
                    drawLine(
                        color = lineColor,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, centerY),
                        strokeWidth = 2.dp.toPx(),
                    )
                }
                if (!isLast) {
                    drawLine(
                        color = lineColor,
                        start = Offset(centerX, centerY),
                        end = Offset(centerX, size.height),
                        strokeWidth = 2.dp.toPx(),
                    )
                }

                drawCircle(
                    color = if (style.isGlass && !isCurrentNow && !hasAnyChange) {
                        Color.White.copy(alpha = 0.45f)
                    } else {
                        timelineDotColor.copy(alpha = if (isCurrentNow) 0.56f else 0.28f)
                    },
                    radius = if (isCurrentNow) 10.dp.toPx() else if (style.isGlass) 8.5.dp.toPx() else 7.dp.toPx(),
                    center = Offset(centerX, centerY),
                )
                drawCircle(
                    color = timelineDotColor,
                    radius = if (isCurrentNow) 6.dp.toPx() else 5.dp.toPx(),
                    center = Offset(centerX, centerY),
                )
                drawCircle(
                    color = if (isCurrentNow) timelineDotColor else Color.White,
                    radius = if (style.isGlass && !isCurrentNow && !hasAnyChange) 3.5.dp.toPx() else 2.5.dp.toPx(),
                    center = Offset(centerX, centerY),
                )
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Резервируем место под вертикальную ось таймлайна
        Spacer(modifier = Modifier.width(22.dp))

        // Колонка времени начала и конца пары
        val timeParts = slot.timeRangeText.split("–").map { it.trim() }
        Column(
            modifier = Modifier
                .width(50.dp)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            if (timeParts.size == 2) {
                Text(
                    text = timeParts[0],
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isCurrentNow -> activeNowAccent
                        style.isNeon -> NeonPalette.textPrimary
                        style.isGlass -> GlassPalette.textPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeParts[1],
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isCurrentNow -> activeNowAccent.copy(alpha = 0.85f)
                        style.isNeon -> NeonPalette.textSecondary
                        style.isGlass -> GlassPalette.textSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            } else {
                Text(
                    text = slot.timeRangeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isCurrentNow -> activeNowAccent
                        style.isNeon -> NeonPalette.textPrimary
                        style.isGlass -> GlassPalette.textPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Правая стеклянная карточка пары (единая иконка даже при нескольких подгруппах)
        val cardFillBrush = when {
            isCurrentNow && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(NeonPalette.activeLessonStart, NeonPalette.activeLessonEnd),
            )
            isCurrentNow && style.isGlass -> Brush.linearGradient(
                colors = listOf(GlassPalette.activeLessonStart, GlassPalette.activeLessonEnd),
            )
            isCurrentNow -> Brush.horizontalGradient(
                colors = listOf(
                    if (isDark) ScheduleAccentColors.currentLessonBgDark else ScheduleAccentColors.currentLessonBgLight,
                    if (isDark) ScheduleAccentColors.currentLessonBgDark else ScheduleAccentColors.currentLessonBgLight,
                ),
            )
            allChanges && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(Color(0xBD2A1B08), Color(0xB31C1206)),
            )
            allChanges && style.isGlass -> Brush.linearGradient(
                colors = listOf(Color(0xA6362612), Color(0x941E150A)),
            )
            hasAnyChange && !style.isTranslucent -> Brush.horizontalGradient(
                colors = listOf(
                    if (isDark) ScheduleAccentColors.changeBgDark else ScheduleAccentColors.changeBgLight,
                    if (isDark) ScheduleAccentColors.changeBgDark else ScheduleAccentColors.changeBgLight,
                ),
            )
            hasAnyChange && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(Color(0xB5231B12), Color(0xA614162E)),
            )
            hasAnyChange && style.isGlass -> Brush.linearGradient(
                colors = listOf(Color(0x9E302416), Color(0x8C1B1814)),
            )
            style.isNeon -> Brush.verticalGradient(
                colors = listOf(Color(0xAE1D254B), Color(0x9E131935)),
            )
            style.isGlass -> Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to GlassPalette.cardFillTop,
                    0.48f to GlassPalette.cardFillMid,
                    1.0f to GlassPalette.cardFillBottom,
                ),
            )
            else -> Brush.verticalGradient(
                colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface),
            )
        }

        val cardBorderBrush = when {
            isCurrentNow -> Brush.horizontalGradient(
                colors = listOf(activeNowAccent, if (style.isTranslucent) GlassPalette.activeLessonAccent else activeNowAccent),
            )
            hasAnyChange -> if (isDark || style.isTranslucent) {
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF925812), Color(0xFF78430C)),
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
                )
            }
            style.isNeon -> Brush.linearGradient(
                colors = listOf(NeonPalette.glassBorderTop, NeonPalette.glassBorderBottom),
            )
            style.isGlass -> Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to GlassPalette.cardBorderTop,
                    0.48f to GlassPalette.cardBorderMid,
                    1.0f to GlassPalette.cardBorderBottom,
                ),
            )
            else -> Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant),
            )
        }

        val glowColor = when {
            !style.isTranslucent -> null
            isCurrentNow -> NeonPalette.activeLessonBorder
            hasAnyChange -> Color(0xFF78430C)
            else -> null
        }

        GlassCard(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp),
            cornerRadius = if (style.isGlass) 22.dp else 18.dp,
            fillBrush = cardFillBrush,
            borderBrush = cardBorderBrush,
            borderWidth = if (isCurrentNow || hasAnyChange) 1.6.dp else 1.dp,
            glowColor = glowColor,
        ) {
            val isMultiSubgroup = slot.lessons.size > 1
            Column(modifier = Modifier.fillMaxWidth()) {
                slot.lessons.forEachIndexed { index, lesson ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            thickness = 1.dp,
                            color = when {
                                isCurrentNow -> activeNowAccent.copy(alpha = 0.35f)
                                style.isNeon -> Color(0x33FFFFFF)
                                style.isGlass -> Color(0x38F4F4F4)
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                        )
                    }

                    SubgroupLessonRow(
                        lesson = lesson,
                        subgroupIndex = index,
                        isMultiSubgroup = isMultiSubgroup,
                        isCurrentNow = isCurrentNow,
                        showNowBadge = isCurrentNow && index == 0,
                        activeNowAccent = activeNowAccent,
                        showGroupsInsteadOfTeachers = showGroupsInsteadOfTeachers,
                    )
                }

                if (slot.startTime != null && slot.endTime != null) {
                    PairProgressFooter(
                        startTime = slot.startTime,
                        endTime = slot.endTime,
                        pairNumber = slot.pairNumber,
                        isToday = isToday,
                        isPastDay = isPastDay,
                        nowTime = nowTime,
                        isCurrentNow = isCurrentNow,
                        activeNowAccent = activeNowAccent,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubgroupLessonRow(
    lesson: Lesson,
    subgroupIndex: Int,
    isMultiSubgroup: Boolean,
    isCurrentNow: Boolean,
    showNowBadge: Boolean,
    activeNowAccent: Color,
    showGroupsInsteadOfTeachers: Boolean,
) {
    val style = LocalAppVisualStyle.current
    val isDark = style.isDark
    val subgroupNumber = ScheduleEngine.extractSubgroupNumber(lesson)
    val showSubgroupChip = isMultiSubgroup || subgroupNumber != null
    val displaySubject = if (showSubgroupChip) {
        ScheduleEngine.cleanSubgroupPrefix(lesson.subject)
    } else {
        lesson.subject
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = if (isMultiSubgroup) 10.dp else 12.dp,
                bottom = 8.dp,
            ),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            style.isNeon -> Color(0xFF28325C)
                            style.isGlass -> GlassPalette.typePillBg
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                    )
                    .then(
                        if (style.isNeon) {
                            Modifier.border(
                                width = 0.75.dp,
                                color = Color.White.copy(alpha = if (isCurrentNow) 0.35f else 0.16f),
                                shape = RoundedCornerShape(10.dp),
                            )
                        } else if (isCurrentNow && style.isGlass) {
                            Modifier.border(
                                width = 0.75.dp,
                                color = Color.White.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(
                    text = lesson.lessonType.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        style.isNeon -> Color(0xFFE2E8F0)
                        style.isGlass -> GlassPalette.typePillText
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    },
                )
            }

            if (showSubgroupChip) {
                val subLabel = if (subgroupNumber != null) {
                    "$subgroupNumber подгр."
                } else {
                    "Подгр. ${subgroupIndex + 1}"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                style.isNeon -> Color(0x33A78BFA)
                                style.isGlass -> GlassPalette.secondaryPillBg
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                        )
                        .border(
                            width = 0.75.dp,
                            color = when {
                                style.isNeon -> Color(0x66C4B5FD)
                                style.isGlass -> Color(0x40F4F4F4)
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(10.dp),
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            style.isNeon -> Color(0xFFE9D5FF)
                            style.isGlass -> GlassPalette.secondaryPillText
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }

            if (lesson.isChange) {
                val changeBg = if (style.isTranslucent || isDark) {
                    ScheduleAccentColors.changeBadgeBgDark
                } else {
                    Color(0xFFF59E0B).copy(alpha = 0.16f)
                }
                val changeContent = if (style.isTranslucent || isDark) {
                    ScheduleAccentColors.changeBadgeDark
                } else {
                    ScheduleAccentColors.changeBadgeLight
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = changeBg,
                    contentColor = changeContent,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = changeContent,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = "ЗАМЕНА",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            if (showNowBadge) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(activeNowAccent.copy(alpha = if (style.isTranslucent) 0.28f else 0.16f))
                        .border(
                            width = 0.75.dp,
                            color = activeNowAccent.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(10.dp),
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "СЕЙЧАС",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (style.isTranslucent || isDark) activeNowAccent else ScheduleAccentColors.currentLessonBorderLight,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = displaySubject,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = when {
                style.isNeon -> NeonPalette.textPrimary
                style.isGlass -> GlassPalette.textPrimary
                else -> MaterialTheme.colorScheme.onSurface
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (lesson.room.isNotBlank()) {
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Image(
                    painter = painterResource(
                        id = if (style.isGlass) R.drawable.ic_glass_room else R.drawable.ic_neon_room,
                    ),
                    contentDescription = null,
                    colorFilter = when {
                        style.isNeon -> null
                        style.isGlass -> ColorFilter.tint(GlassPalette.iconTint)
                        else -> ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Ауд. ${lesson.room}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        style.isNeon -> NeonPalette.textSecondary
                        style.isGlass -> GlassPalette.textSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        val showGroups = showGroupsInsteadOfTeachers && lesson.groups.isNotEmpty()
        if (showGroups) {
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_neon_group),
                    contentDescription = null,
                    colorFilter = when {
                        style.isNeon -> null
                        style.isGlass -> ColorFilter.tint(GlassPalette.iconTint)
                        else -> ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = lesson.groups.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        style.isNeon -> NeonPalette.textSecondary
                        style.isGlass -> GlassPalette.textSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else if (lesson.teachers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_neon_teacher),
                    contentDescription = null,
                    colorFilter = when {
                        style.isNeon -> null
                        style.isGlass -> ColorFilter.tint(GlassPalette.iconTint)
                        else -> ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = lesson.teachers.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        style.isNeon -> NeonPalette.textSecondary
                        style.isGlass -> GlassPalette.textSecondary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PairProgressFooter(
    startTime: LocalTime,
    endTime: LocalTime,
    pairNumber: Int,
    isToday: Boolean,
    isPastDay: Boolean,
    nowTime: LocalTime,
    isCurrentNow: Boolean,
    activeNowAccent: Color,
) {
    val style = LocalAppVisualStyle.current
    val totalSeconds = Duration.between(startTime, endTime).seconds.coerceAtLeast(1L)
    val totalMinutes = Duration.between(startTime, endTime).toMinutes().coerceAtLeast(1L)

    val isFinished = isPastDay || (isToday && !nowTime.isBefore(endTime))
    val isRunning = isCurrentNow || (isToday && !nowTime.isBefore(startTime) && nowTime.isBefore(endTime))
    val isUpcomingToday = isToday && nowTime.isBefore(startTime)

    val targetProgress: Float
    val timerLabel: String
    when {
        isFinished -> {
            targetProgress = 1f
            timerLabel = "Пара закончилась"
        }
        isRunning -> {
            val elapsed = Duration.between(startTime, nowTime).seconds.coerceIn(0L, totalSeconds)
            val remainingMin = ((totalSeconds - elapsed + 59L) / 60L).coerceAtLeast(1L)
            targetProgress = (elapsed.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
            timerLabel = "До конца: ${formatDurationMin(remainingMin)}"
        }
        isUpcomingToday -> {
            val untilMin = Duration.between(nowTime, startTime).toMinutes().coerceAtLeast(1L)
            targetProgress = 0f
            timerLabel = "Начнётся через ${formatDurationMin(untilMin)}"
        }
        else -> {
            targetProgress = 0f
            timerLabel = "Длительность: ${formatDurationMin(totalMinutes)}"
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 200),
        label = "pairCardProgress",
    )

    val labelColor = when {
        isRunning -> activeNowAccent
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = timerLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isRunning) FontWeight.Bold else FontWeight.Medium,
                color = labelColor,
            )
            if (isRunning) {
                Text(
                    text = "${(targetProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = activeNowAccent,
                )
            } else if (pairNumber > 0) {
                Text(
                    text = "$pairNumber пара",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor.copy(alpha = 0.8f),
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    when {
                        style.isNeon -> labelColor.copy(alpha = if (isRunning) 0.32f else 0.65f)
                        style.isGlass -> GlassPalette.progressTrack
                        style.isDark -> labelColor.copy(alpha = if (isRunning) 0.35f else 1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
        ) {
            if (animatedProgress > 0f) {
                val fillBrush = when {
                    isRunning -> Brush.horizontalGradient(
                        colors = listOf(activeNowAccent, if (style.isTranslucent) NeonPalette.activeLessonAccent else activeNowAccent),
                    )
                    isFinished && style.isNeon -> Brush.horizontalGradient(
                        colors = listOf(Color(0x88A855F7), Color(0x8838BDF8)),
                    )
                    isFinished && style.isGlass -> Brush.horizontalGradient(
                        colors = listOf(GlassPalette.progressFillStart, GlassPalette.progressFillEnd),
                    )
                    else -> Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceAtLeast(0.03f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(fillBrush),
                )
            }
        }
    }
}

private fun formatDurationMin(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h} ч ${m} мин" else "$m мин"
}
