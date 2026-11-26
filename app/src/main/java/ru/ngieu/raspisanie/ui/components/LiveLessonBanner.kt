package ru.ngieu.raspisanie.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ngieu.raspisanie.R
import ru.ngieu.raspisanie.domain.LessonSlot
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.domain.ScheduleEngine
import ru.ngieu.raspisanie.ui.DayProgressSummary
import ru.ngieu.raspisanie.ui.theme.AeroPalette
import ru.ngieu.raspisanie.ui.theme.AppVisualSkin
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.TerminalPalette
import java.time.format.DateTimeFormatter

private val hmFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun LiveLessonBanner(
    state: LiveLessonState,
    progressSummary: DayProgressSummary,
    isViewingToday: Boolean,
    daySlots: List<LessonSlot> = emptyList(),
    modifier: Modifier = Modifier,
) {
    if (progressSummary.totalCount == 0 && (!isViewingToday || state is LiveLessonState.NoLessonsToday)) {
        return
    }

    val style = LocalAppVisualStyle.current
    var expanded by remember { mutableStateOf(false) }

    val targetFraction = when {
        isViewingToday && state is LiveLessonState.InProgress -> {
            val base = (progressSummary.activeCount - 1).coerceAtLeast(0).toFloat()
            ((base + state.progress.coerceIn(0f, 1f)) / progressSummary.totalCount.coerceAtLeast(1)).coerceIn(0f, 1f)
        }
        else -> progressSummary.fraction
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 250),
        label = "dayProgress",
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "chevronRotation",
    )

    val capsuleBrush = when {
        style.isNeon -> Brush.horizontalGradient(
            colors = listOf(
                Color(0xBD242958),
                Color(0xB319224B),
            ),
        )
        style.isGlass -> Brush.verticalGradient(
            colors = listOf(
                GlassPalette.panelFillTop,
                GlassPalette.panelFillBottom,
            ),
        )
        style.isAero -> AeroPalette.panelFillBrush()
        style.isTerminal -> TerminalPalette.panelFillBrush()
        else -> Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
    }

    val borderBrush = when {
        style.isNeon -> Brush.horizontalGradient(
            colors = listOf(
                Color(0x59A855F7),
                Color(0x5938BDF8),
            ),
        )
        style.isGlass -> Brush.verticalGradient(
            colors = listOf(
                GlassPalette.panelBorderTop,
                GlassPalette.panelBorderBottom,
            ),
        )
        style.isAero -> AeroPalette.glassBorderBrush()
        style.isTerminal -> TerminalPalette.glassBorderBrush()
        else -> Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.outlineVariant,
                MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }

    val canExpand = (isViewingToday && state !is LiveLessonState.NoLessonsToday) || daySlots.isNotEmpty()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(130)),
        cornerRadius = when {
            style.isGlass -> 22.dp
            style.isAero -> 16.dp
            style.isTerminal -> 14.dp
            else -> 20.dp
        },
        fillBrush = capsuleBrush,
        borderBrush = borderBrush,
        glowColor = when {
            style.isNeon -> Color(0xFF7C3AED)
            style.isGlass -> Color.White
            style.isAero -> Color(0xFF38BDF8)
            style.isTerminal -> TerminalPalette.glowGreen
            else -> null
        },
        onClick = if (canExpand) {
            { expanded = !expanded }
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val word = pairsWord(progressSummary.totalCount)
                    val captionSuffix = if (isViewingToday) "$word сегодня" else "$word в этот день"
                    val primaryTextColor = when {
                        style.isNeon -> NeonPalette.textPrimary
                        style.isGlass -> GlassPalette.textPrimary
                        style.isAero -> AeroPalette.textPrimary
                        style.isTerminal -> TerminalPalette.textPrimary
                        else -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                    val secondaryTextColor = when {
                        style.isNeon -> NeonPalette.textSecondary
                        style.isGlass -> GlassPalette.textSecondary
                        style.isAero -> AeroPalette.textSecondary
                        style.isTerminal -> TerminalPalette.textSecondary
                        else -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    }

                    Text(
                        text = buildAnnotatedString {
                            if (isViewingToday || progressSummary.activeCount > 0) {
                                withStyle(
                                    SpanStyle(
                                        fontSize = 21.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryTextColor,
                                    ),
                                ) {
                                    append("${progressSummary.activeCount}")
                                }
                                withStyle(
                                    SpanStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = secondaryTextColor,
                                    ),
                                ) {
                                    append(" / ${progressSummary.totalCount} ")
                                }
                                withStyle(
                                    SpanStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = primaryTextColor.copy(alpha = 0.92f),
                                    ),
                                ) {
                                    append(captionSuffix)
                                }
                            } else {
                                withStyle(
                                    SpanStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryTextColor,
                                    ),
                                ) {
                                    append("${progressSummary.totalCount} $captionSuffix")
                                }
                            }
                        },
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Полоса прогресса
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (style.isAero) 7.dp else 6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    style.isNeon -> Color(0x38FFFFFF)
                                    style.isGlass -> GlassPalette.progressTrack
                                    style.isAero -> AeroPalette.progressTrack
                                    style.isTerminal -> TerminalPalette.progressTrack
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                            ),
                    ) {
                        if (animatedProgress > 0f) {
                            val progressBrush = when {
                                style.isNeon -> Brush.horizontalGradient(listOf(NeonPalette.progressStart, NeonPalette.progressEnd))
                                style.isGlass -> Brush.horizontalGradient(listOf(GlassPalette.progressFillStart, GlassPalette.progressFillEnd))
                                style.isAero -> AeroPalette.progressFillBrush()
                                style.isTerminal -> TerminalPalette.progressFillBrush()
                                else -> Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress.coerceAtLeast(0.04f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(brush = progressBrush),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(12.dp))

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(
                            brush = when {
                                style.isAero -> AeroPalette.panelFillBrush()
                                style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.panelFillTop, GlassPalette.panelFillTop))
                                style.isTerminal -> TerminalPalette.panelFillBrush()
                                style.isNeon -> Brush.verticalGradient(listOf(Color(0x28FFFFFF), Color(0x28FFFFFF)))
                                else -> Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                    ),
                                )
                            },
                            shape = CircleShape,
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                style.isNeon -> Color(0x30FFFFFF)
                                style.isGlass -> GlassPalette.panelBorderTop
                                style.isAero -> AeroPalette.borderTop
                                style.isTerminal -> TerminalPalette.borderTop
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_neon_chevron_right),
                        contentDescription = "Подробнее о прогрессе дня",
                        colorFilter = when {
                            style.isNeon -> null
                            style.isGlass -> ColorFilter.tint(Color(0xFFF5F5F5))
                            style.isAero -> ColorFilter.tint(Color.White)
                            style.isTerminal -> ColorFilter.tint(TerminalPalette.iconTint)
                            else -> ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer)
                        },
                        modifier = Modifier
                            .size(15.dp)
                            .rotate(chevronRotation),
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded && canExpand,
                enter = expandVertically(tween(120)) + fadeIn(tween(120)),
                exit = shrinkVertically(tween(100)) + fadeOut(tween(100)),
            ) {
                if (isViewingToday && state !is LiveLessonState.NoLessonsToday) {
                    AnimatedContent(
                        targetState = state,
                        transitionSpec = { fadeIn(tween(110)) togetherWith fadeOut(tween(90)) },
                        label = "liveDetail",
                    ) { live ->
                        LiveDetailsStrip(state = live, skin = style.skin)
                    }
                } else if (daySlots.isNotEmpty()) {
                    DaySummaryStrip(daySlots = daySlots, skin = style.skin)
                }
            }
        }
    }
}

@Composable
private fun DaySummaryStrip(
    daySlots: List<LessonSlot>,
    skin: AppVisualSkin,
) {
    val textColor = when (skin) {
        AppVisualSkin.NEON -> NeonPalette.textPrimary
        AppVisualSkin.GLASS -> GlassPalette.textPrimary
        AppVisualSkin.AERO -> AeroPalette.textPrimary
        AppVisualSkin.TERMINAL -> TerminalPalette.textPrimary
        AppVisualSkin.STANDARD -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val subColor = when (skin) {
        AppVisualSkin.NEON -> NeonPalette.textSecondary
        AppVisualSkin.GLASS -> GlassPalette.textSecondary
        AppVisualSkin.AERO -> AeroPalette.textSecondary
        AppVisualSkin.TERMINAL -> TerminalPalette.textSecondary
        AppVisualSkin.STANDARD -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
    }

    val firstSlot = daySlots.first()
    val firstStart = daySlots.firstNotNullOfOrNull { it.startTime }
    val lastEnd = daySlots.lastOrNull { it.endTime != null }?.endTime
    val changesCount = daySlots.count { slot -> slot.lessons.any { it.isChange } }

    val timeSpanText = if (firstStart != null && lastEnd != null) {
        "Занятия с ${firstStart.format(hmFormatter)} до ${lastEnd.format(hmFormatter)}"
    } else {
        "Начало: ${firstSlot.timeRangeText}"
    }
    val firstLesson = firstSlot.lessons.firstOrNull()
    val firstSubject = firstLesson?.let { ScheduleEngine.cleanSubgroupPrefix(it.subject) }.orEmpty()
    val secondLine = buildString {
        if (firstSubject.isNotEmpty()) {
            append("Первая пара: $firstSubject")
        }
        if (changesCount > 0) {
            if (isNotEmpty()) append(" • ")
            append("Замен: $changesCount")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Rounded.Schedule,
            contentDescription = null,
            tint = when (skin) {
                AppVisualSkin.NEON -> Color(0xFF38BDF8)
                AppVisualSkin.GLASS -> Color(0xFFF5F5F5)
                AppVisualSkin.AERO -> Color(0xFFEAF8FF)
                AppVisualSkin.TERMINAL -> TerminalPalette.iconTint
                AppVisualSkin.STANDARD -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeSpanText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
            if (secondLine.isNotEmpty()) {
                Text(
                    text = secondLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun LiveDetailsStrip(
    state: LiveLessonState,
    skin: AppVisualSkin,
) {
    val textColor = when (skin) {
        AppVisualSkin.NEON -> NeonPalette.textPrimary
        AppVisualSkin.GLASS -> GlassPalette.textPrimary
        AppVisualSkin.AERO -> AeroPalette.textPrimary
        AppVisualSkin.TERMINAL -> TerminalPalette.textPrimary
        AppVisualSkin.STANDARD -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    val subColor = when (skin) {
        AppVisualSkin.NEON -> NeonPalette.textSecondary
        AppVisualSkin.GLASS -> GlassPalette.textSecondary
        AppVisualSkin.AERO -> AeroPalette.textSecondary
        AppVisualSkin.TERMINAL -> TerminalPalette.textSecondary
        AppVisualSkin.STANDARD -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
    }

    val (icon, title, subtitle) = when (state) {
        is LiveLessonState.InProgress -> Triple(
            Icons.Rounded.PlayCircle,
            "Сейчас ${state.currentLesson.pairNumber} пара • осталось ${state.minutesRemaining} мин",
            buildString {
                append(ScheduleEngine.cleanSubgroupPrefix(state.currentLesson.subject))
                if (state.currentLesson.room.isNotBlank()) append(" • ауд. ${state.currentLesson.room}")
            },
        )
        is LiveLessonState.BeforeFirstLesson -> Triple(
            Icons.Rounded.Schedule,
            "До первой пары ${formatMinutes(state.minutesUntilStart)}",
            "${ScheduleEngine.cleanSubgroupPrefix(state.nextLesson.subject)} (${state.nextLesson.timeRangeText})",
        )
        is LiveLessonState.OnBreak -> Triple(
            Icons.Rounded.Schedule,
            "Перемена • через ${formatMinutes(state.minutesUntilStart)} ${state.nextLesson.pairNumber} пара",
            buildString {
                append(ScheduleEngine.cleanSubgroupPrefix(state.nextLesson.subject))
                if (state.nextLesson.room.isNotBlank()) append(" • ауд. ${state.nextLesson.room}")
            },
        )
        LiveLessonState.FinishedForToday -> Triple(
            Icons.Rounded.CheckCircle,
            "Пары на сегодня закончились",
            "Можно отдохнуть или свайпнуть на завтра",
        )
        LiveLessonState.NoLessonsToday -> return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when (skin) {
                AppVisualSkin.NEON -> Color(0xFF38BDF8)
                AppVisualSkin.GLASS -> Color(0xFFF5F5F5)
                AppVisualSkin.AERO -> Color(0xFFEAF8FF)
                AppVisualSkin.TERMINAL -> TerminalPalette.iconTint
                AppVisualSkin.STANDARD -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun pairsWord(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "пара"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "пары"
        else -> "пар"
    }
}

private fun formatMinutes(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h} ч ${m} мин" else "$m мин"
}
