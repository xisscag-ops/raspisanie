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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.ngieu.raspisanie.R
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.data.ThemeModeSetting
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.ui.theme.AeroPalette
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors
import ru.ngieu.raspisanie.ui.theme.TerminalPalette
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
    val isStandardDark = !style.isTranslucent && isDark
    val hasAnyChange = slot.lessons.any { it.isChange }
    val allChanges = slot.lessons.all { it.isChange }
    val hasAnyCancelled = slot.lessons.any { it.isCancelled }
    val allCancelled = slot.lessons.all { it.isCancelled }

    val activeNowAccent = when {
        style.isGlass -> GlassPalette.timelineActiveNow
        style.isAero -> AeroPalette.timelineActiveNow
        style.isTerminal -> TerminalPalette.timelineActiveNow
        style.isNeon || isDark -> NeonPalette.timelineActiveNow
        else -> ScheduleAccentColors.currentLessonBorderLight
    }

    val timelineDotColor by animateColorAsState(
        targetValue = when {
            isCurrentNow -> activeNowAccent
            hasAnyCancelled -> when {
                isStandardDark -> ScheduleAccentColors.cancelBadgeDark
                style.isTranslucent -> ScheduleAccentColors.cancelBorderNeon
                else -> ScheduleAccentColors.cancelBorderLight
            }
            hasAnyChange -> if (isStandardDark) ScheduleAccentColors.changeBadgeDark else Color(0xFFFBBF24)
            style.isNeon -> NeonPalette.timelinePurple
            style.isGlass -> GlassPalette.timelineDot
            style.isAero -> AeroPalette.timelineDot
            style.isTerminal -> TerminalPalette.timelineDot
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(110),
        label = "timelineDot",
    )

    val lineColor = when {
        style.isNeon -> Color(0xFF7C3AED).copy(alpha = 0.72f)
        style.isGlass -> GlassPalette.timelineLine
        style.isAero -> Color(0xFFD0F0FF).copy(alpha = 0.90f)
        style.isTerminal -> TerminalPalette.timelineLine
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
                    color = when {
                        style.isAero && !isCurrentNow && !hasAnyChange && !hasAnyCancelled ->
                            AeroPalette.timelineDotGlow
                        style.isGlass && !isCurrentNow && !hasAnyChange && !hasAnyCancelled ->
                            Color.White.copy(alpha = 0.45f)
                        style.isTerminal && !isCurrentNow && !hasAnyChange && !hasAnyCancelled ->
                            TerminalPalette.timelineDotGlow
                        else ->
                            timelineDotColor.copy(alpha = if (isCurrentNow) 0.56f else 0.28f)
                    },
                    radius = when {
                        isCurrentNow -> 10.dp.toPx()
                        style.isGlass || style.isAero || style.isTerminal -> 8.5.dp.toPx()
                        else -> 7.dp.toPx()
                    },
                    center = Offset(centerX, centerY),
                )
                drawCircle(
                    color = timelineDotColor,
                    radius = if (isCurrentNow) 6.dp.toPx() else 5.dp.toPx(),
                    center = Offset(centerX, centerY),
                )
                drawCircle(
                    color = when {
                        isCurrentNow -> timelineDotColor
                        style.isTerminal -> Color(0xFFD8FFD2)
                        else -> Color.White
                    },
                    radius = if ((style.isGlass || style.isAero || style.isTerminal) && !isCurrentNow && !hasAnyChange && !hasAnyCancelled) 3.2.dp.toPx() else 2.5.dp.toPx(),
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
                        style.isAero -> AeroPalette.textPrimary
                        style.isTerminal -> TerminalPalette.textPrimary
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
                        style.isAero -> AeroPalette.textSecondary
                        style.isTerminal -> TerminalPalette.textSecondary
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
                        style.isAero -> AeroPalette.textPrimary
                        style.isTerminal -> TerminalPalette.textPrimary
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
            isCurrentNow && style.isAero -> Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to AeroPalette.activeLessonTop,
                    0.48f to AeroPalette.activeLessonMid,
                    1.00f to AeroPalette.activeLessonBottom,
                ),
            )
            isCurrentNow && style.isTerminal -> Brush.verticalGradient(
                colors = listOf(TerminalPalette.activeLessonTop, TerminalPalette.activeLessonBottom),
            )
            isCurrentNow -> Brush.horizontalGradient(
                colors = listOf(
                    if (isDark) ScheduleAccentColors.currentLessonBgDark else ScheduleAccentColors.currentLessonBgLight,
                    if (isDark) ScheduleAccentColors.currentLessonBgDark else ScheduleAccentColors.currentLessonBgLight,
                ),
            )
            allCancelled && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(ScheduleAccentColors.cancelBgNeonStart, ScheduleAccentColors.cancelBgNeonEnd),
            )
            allCancelled && style.isGlass -> Brush.linearGradient(
                colors = listOf(ScheduleAccentColors.cancelBgGlassStart, ScheduleAccentColors.cancelBgGlassEnd),
            )
            allCancelled && style.isAero -> Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to AeroPalette.cancelLessonTop,
                    0.48f to AeroPalette.cancelLessonMid,
                    1.00f to AeroPalette.cancelLessonBottom,
                ),
            )
            allCancelled && style.isTerminal -> Brush.verticalGradient(
                colors = listOf(TerminalPalette.cancelLessonTop, TerminalPalette.cancelLessonBottom),
            )
            allCancelled && !style.isTranslucent -> Brush.horizontalGradient(
                colors = listOf(
                    if (isDark) ScheduleAccentColors.cancelBgDark else ScheduleAccentColors.cancelBgLight,
                    if (isDark) ScheduleAccentColors.cancelBgDark else ScheduleAccentColors.cancelBgLight,
                ),
            )
            allChanges && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(Color(0xBD3D260A), Color(0xB3271907)),
            )
            allChanges && style.isGlass -> Brush.linearGradient(
                colors = listOf(Color(0xA64E3818), Color(0x94291D0C)),
            )
            allChanges && style.isAero -> Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to AeroPalette.changeLessonTop,
                    0.48f to AeroPalette.changeLessonMid,
                    1.00f to AeroPalette.changeLessonBottom,
                ),
            )
            allChanges && style.isTerminal -> Brush.verticalGradient(
                colors = listOf(TerminalPalette.changeLessonTop, TerminalPalette.changeLessonBottom),
            )
            hasAnyChange && !style.isTranslucent -> Brush.horizontalGradient(
                colors = listOf(
                    if (isDark) MaterialTheme.colorScheme.surface else ScheduleAccentColors.changeBgLight,
                    if (isDark) MaterialTheme.colorScheme.surface else ScheduleAccentColors.changeBgLight,
                ),
            )
            hasAnyChange && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(Color(0xB52E2318), Color(0xA6191B38)),
            )
            hasAnyChange && style.isGlass -> Brush.linearGradient(
                colors = listOf(Color(0x9E45351F), Color(0x8C23201B)),
            )
            hasAnyChange && style.isAero -> Brush.verticalGradient(
                colorStops = arrayOf(
                    0.00f to Color(0xDC7A6228),
                    0.48f to Color(0xDF334D59),
                    1.00f to Color(0xE03A5C6A),
                ),
            )
            hasAnyChange && style.isTerminal -> Brush.verticalGradient(
                colors = listOf(Color(0xDC222A0B), Color(0xE40E1A09)),
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
            style.isAero -> AeroPalette.cardFillBrush()
            style.isTerminal -> TerminalPalette.cardFillBrush()
            else -> Brush.verticalGradient(
                colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface),
            )
        }

        val cardBorderBrush = when {
            isCurrentNow -> Brush.horizontalGradient(
                colors = listOf(
                    activeNowAccent,
                    when {
                        style.isTerminal -> TerminalPalette.activeLessonAccent
                        style.isTranslucent -> GlassPalette.activeLessonAccent
                        else -> activeNowAccent
                    },
                ),
            )
            hasAnyCancelled -> when {
                isStandardDark -> Brush.horizontalGradient(
                    colors = listOf(ScheduleAccentColors.cancelBorderDark, Color(0xFF6B2028)),
                )
                style.isTranslucent -> Brush.horizontalGradient(
                    colors = listOf(ScheduleAccentColors.cancelBorderNeon, Color(0xFFEF4444)),
                )
                else -> Brush.horizontalGradient(
                    colors = listOf(ScheduleAccentColors.cancelBorderLight, ScheduleAccentColors.cancelBorderLight),
                )
            }
            hasAnyChange -> if (isStandardDark) {
                Brush.horizontalGradient(
                    colors = listOf(ScheduleAccentColors.changeBorderDark, Color(0xFF78430C)),
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
            style.isAero -> AeroPalette.glassBorderBrush()
            style.isTerminal -> TerminalPalette.glassBorderBrush()
            else -> Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant),
            )
        }

        val glowColor = when {
            !style.isTranslucent -> null
            isCurrentNow -> if (style.isTerminal) TerminalPalette.glowGreen else NeonPalette.activeLessonBorder
            hasAnyCancelled -> Color(0xFFEF4444)
            hasAnyChange -> Color(0xFFF59E0B)
            style.isAero -> Color(0xFF38BDF8)
            style.isTerminal -> TerminalPalette.glowGreen
            else -> null
        }

        GlassCard(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp),
            cornerRadius = when {
                style.isGlass -> 22.dp
                style.isAero -> 16.dp
                style.isTerminal -> 14.dp
                else -> 18.dp
            },
            fillBrush = cardFillBrush,
            borderBrush = cardBorderBrush,
            borderWidth = if (isCurrentNow || hasAnyChange || hasAnyCancelled) 1.6.dp else 1.dp,
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
                                style.isAero -> Color(0x52BCEFFE)
                                style.isTerminal -> TerminalPalette.divider
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
                        isCancelled = allCancelled,
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
            val pillShape = if (style.isTerminal) CutCornerShape(6.dp) else RoundedCornerShape(10.dp)
            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(
                        brush = when {
                            style.isNeon -> Brush.verticalGradient(listOf(Color(0xFF28325C), Color(0xFF28325C)))
                            style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.typePillBg, GlassPalette.typePillBg))
                            style.isAero -> Brush.verticalGradient(listOf(AeroPalette.typePillFillTop, AeroPalette.typePillFillBottom))
                            style.isTerminal -> Brush.verticalGradient(listOf(TerminalPalette.typePillBgTop, TerminalPalette.typePillBgBottom))
                            else -> Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            )
                        },
                    )
                    .then(
                        when {
                            style.isNeon -> Modifier.border(
                                width = 0.75.dp,
                                color = Color.White.copy(alpha = if (isCurrentNow) 0.35f else 0.16f),
                                shape = pillShape,
                            )
                            style.isAero -> Modifier.border(
                                width = 0.9.dp,
                                color = AeroPalette.typePillBorder,
                                shape = pillShape,
                            )
                            style.isTerminal -> Modifier.border(
                                width = 0.9.dp,
                                color = TerminalPalette.typePillBorder,
                                shape = pillShape,
                            )
                            isCurrentNow && style.isGlass -> Modifier.border(
                                width = 0.75.dp,
                                color = Color.White.copy(alpha = 0.35f),
                                shape = pillShape,
                            )
                            else -> Modifier
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
                        style.isAero -> AeroPalette.typePillText
                        style.isTerminal -> TerminalPalette.typePillText
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
                        .clip(pillShape)
                        .background(
                            when {
                                style.isNeon -> Color(0x33A78BFA)
                                style.isGlass -> GlassPalette.secondaryPillBg
                                style.isAero -> AeroPalette.secondaryPillBg
                                style.isTerminal -> TerminalPalette.secondaryPillBg
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                        )
                        .border(
                            width = 0.75.dp,
                            color = when {
                                style.isNeon -> Color(0x66C4B5FD)
                                style.isGlass -> Color(0x40F4F4F4)
                                style.isAero -> AeroPalette.secondaryPillBorder
                                style.isTerminal -> TerminalPalette.secondaryPillBorder
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = pillShape,
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
                            style.isAero -> AeroPalette.secondaryPillText
                            style.isTerminal -> TerminalPalette.secondaryPillText
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        },
                    )
                }
            }

            if (lesson.isChange) {
                val isStandardDark = !style.isTranslucent && isDark
                val changeBg = when {
                    style.isTranslucent -> Color(0xFFF59E0B).copy(alpha = 0.28f)
                    isStandardDark -> ScheduleAccentColors.changeBadgeBgDark
                    else -> Color(0xFFF59E0B).copy(alpha = 0.16f)
                }
                val changeContent = when {
                    style.isTranslucent -> Color(0xFFFBBF24)
                    isStandardDark -> ScheduleAccentColors.changeBadgeDark
                    else -> ScheduleAccentColors.changeBadgeLight
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

            if (lesson.isCancelled) {
                val isStandardDark = !style.isTranslucent && isDark
                val cancelBg = when {
                    style.isTranslucent -> ScheduleAccentColors.cancelBadgeBgNeon
                    isStandardDark -> ScheduleAccentColors.cancelBadgeBgDark
                    else -> ScheduleAccentColors.cancelBadgeBgLight
                }
                val cancelContent = when {
                    style.isTranslucent -> ScheduleAccentColors.cancelBadgeNeon
                    isStandardDark -> ScheduleAccentColors.cancelBadgeDark
                    else -> ScheduleAccentColors.cancelBadgeLight
                }
                val cancelBorder = when {
                    style.isTranslucent -> ScheduleAccentColors.cancelBorderNeon.copy(alpha = 0.45f)
                    isStandardDark -> ScheduleAccentColors.cancelBorderDark.copy(alpha = 0.6f)
                    else -> ScheduleAccentColors.cancelBorderLight.copy(alpha = 0.35f)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(cancelBg)
                        .border(width = 0.75.dp, color = cancelBorder, shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Cancel,
                            contentDescription = null,
                            tint = cancelContent,
                            modifier = Modifier.size(11.dp),
                        )
                        Text(
                            text = "ОТМЕНА",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = cancelContent,
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

        val baseSubjectColor = when {
            style.isNeon -> NeonPalette.textPrimary
            style.isGlass -> GlassPalette.textPrimary
            style.isAero -> AeroPalette.textPrimary
            style.isTerminal -> TerminalPalette.textPrimary
            else -> MaterialTheme.colorScheme.onSurface
        }
        Text(
            text = displaySubject,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (lesson.isCancelled) baseSubjectColor.copy(alpha = 0.68f) else baseSubjectColor,
            textDecoration = if (lesson.isCancelled) TextDecoration.LineThrough else TextDecoration.None,
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
                        id = if (style.isGlass || style.isAero || style.isTerminal) R.drawable.ic_glass_room else R.drawable.ic_neon_room,
                    ),
                    contentDescription = null,
                    colorFilter = when {
                        style.isNeon -> null
                        style.isGlass -> ColorFilter.tint(GlassPalette.iconTint)
                        style.isAero -> ColorFilter.tint(AeroPalette.iconTint)
                        style.isTerminal -> ColorFilter.tint(TerminalPalette.iconTint)
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
                        style.isAero -> AeroPalette.textSecondary
                        style.isTerminal -> TerminalPalette.textSecondary
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
                        style.isAero -> ColorFilter.tint(AeroPalette.iconTint)
                        style.isTerminal -> ColorFilter.tint(TerminalPalette.iconTint)
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
                        style.isAero -> AeroPalette.textSecondary
                        style.isTerminal -> TerminalPalette.textSecondary
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
                        style.isAero -> ColorFilter.tint(AeroPalette.iconTint)
                        style.isTerminal -> ColorFilter.tint(TerminalPalette.iconTint)
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
                        style.isAero -> AeroPalette.textSecondary
                        style.isTerminal -> TerminalPalette.textSecondary
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
    isCancelled: Boolean,
    activeNowAccent: Color,
) {
    val style = LocalAppVisualStyle.current
    val totalSeconds = Duration.between(startTime, endTime).seconds.coerceAtLeast(1L)
    val totalMinutes = Duration.between(startTime, endTime).toMinutes().coerceAtLeast(1L)

    val isFinished = !isCancelled && (isPastDay || (isToday && !nowTime.isBefore(endTime)))
    val isRunning = !isCancelled && (isCurrentNow || (isToday && !nowTime.isBefore(startTime) && nowTime.isBefore(endTime)))
    val isUpcomingToday = !isCancelled && isToday && nowTime.isBefore(startTime)

    val targetProgress: Float
    val timerLabel: String
    when {
        isCancelled -> {
            targetProgress = 0f
            timerLabel = "Пара отменена"
        }
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
        isCancelled -> when {
            !style.isTranslucent && style.isDark -> ScheduleAccentColors.cancelBadgeDark
            style.isTranslucent -> ScheduleAccentColors.cancelBadgeNeon
            else -> ScheduleAccentColors.cancelBadgeLight
        }
        isRunning -> activeNowAccent
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        style.isAero -> AeroPalette.textPrimary.copy(alpha = 0.92f)
        style.isTerminal -> TerminalPalette.textSecondary
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
                .height(if (style.isAero) 5.dp else 4.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(
                    when {
                        style.isNeon -> labelColor.copy(alpha = if (isRunning) 0.32f else 0.65f)
                        style.isGlass -> GlassPalette.progressTrack
                        style.isAero -> AeroPalette.progressTrack
                        style.isTerminal -> TerminalPalette.progressTrack
                        style.isDark -> labelColor.copy(alpha = if (isRunning) 0.35f else 1f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                ),
        ) {
            if (animatedProgress > 0f) {
                val fillBrush = when {
                    isRunning -> Brush.horizontalGradient(
                        colors = listOf(
                            activeNowAccent,
                            when {
                                style.isTerminal -> TerminalPalette.activeLessonAccent
                                style.isTranslucent -> NeonPalette.activeLessonAccent
                                else -> activeNowAccent
                            },
                        ),
                    )
                    isFinished && style.isNeon -> Brush.horizontalGradient(
                        colors = listOf(Color(0x88A855F7), Color(0x8838BDF8)),
                    )
                    isFinished && style.isGlass -> Brush.horizontalGradient(
                        colors = listOf(GlassPalette.progressFillStart, GlassPalette.progressFillEnd),
                    )
                    isFinished && style.isAero -> AeroPalette.progressFillBrush()
                    isFinished && style.isTerminal -> TerminalPalette.progressFillBrush()
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
                        .clip(RoundedCornerShape(3.dp))
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
