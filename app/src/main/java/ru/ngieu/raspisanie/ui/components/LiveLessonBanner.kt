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
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.ui.DayProgressSummary
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette

@Composable
fun LiveLessonBanner(
    state: LiveLessonState,
    progressSummary: DayProgressSummary,
    isViewingToday: Boolean,
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
        animationSpec = tween(durationMillis = 450),
        label = "dayProgress",
    )

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "chevronRotation",
    )

    val capsuleBrush = if (style.isNeon) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xBD242958),
                Color(0xB319224B),
            ),
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.secondaryContainer,
            ),
        )
    }

    val borderBrush = if (style.isNeon) {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0x59A855F7),
                Color(0x5938BDF8),
            ),
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.outlineVariant,
                MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(220)),
        cornerRadius = 20.dp,
        fillBrush = capsuleBrush,
        borderBrush = borderBrush,
        glowColor = if (style.isNeon) Color(0xFF7C3AED) else null,
        onClick = { expanded = !expanded },
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
                    val captionSuffix = if (isViewingToday) "пар сегодня" else "пар в этот день"
                    val primaryTextColor = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                    val secondaryTextColor = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)

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

                    // Неоновая полоса прогресса
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (style.isNeon) Color(0x38FFFFFF) else MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    ) {
                        if (animatedProgress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress.coerceAtLeast(0.04f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = if (style.isNeon) {
                                                listOf(NeonPalette.progressStart, NeonPalette.progressEnd)
                                            } else {
                                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                            },
                                        ),
                                    ),
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
                            if (style.isNeon) Color(0x28FFFFFF) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        )
                        .border(
                            width = 1.dp,
                            color = if (style.isNeon) Color(0x30FFFFFF) else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_neon_chevron_right),
                        contentDescription = "Подробнее о прогрессе дня",
                        colorFilter = if (style.isNeon) null else ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier
                            .size(15.dp)
                            .rotate(chevronRotation),
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded && isViewingToday && state !is LiveLessonState.NoLessonsToday,
                enter = expandVertically(tween(200)) + fadeIn(tween(200)),
                exit = shrinkVertically(tween(160)) + fadeOut(tween(160)),
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                    label = "liveDetail",
                ) { live ->
                    LiveDetailsStrip(state = live, isNeon = style.isNeon)
                }
            }
        }
    }
}

@Composable
private fun LiveDetailsStrip(
    state: LiveLessonState,
    isNeon: Boolean,
) {
    val textColor = if (isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSecondaryContainer
    val subColor = if (isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)

    val (icon, title, subtitle) = when (state) {
        is LiveLessonState.InProgress -> Triple(
            Icons.Rounded.PlayCircle,
            "Сейчас ${state.currentLesson.pairNumber} пара • осталось ${state.minutesRemaining} мин",
            buildString {
                append(state.currentLesson.subject)
                if (state.currentLesson.room.isNotBlank()) append(" • ауд. ${state.currentLesson.room}")
            },
        )
        is LiveLessonState.BeforeFirstLesson -> Triple(
            Icons.Rounded.Schedule,
            "До первой пары ${formatMinutes(state.minutesUntilStart)}",
            "${state.nextLesson.subject} (${state.nextLesson.timeRangeText})",
        )
        is LiveLessonState.OnBreak -> Triple(
            Icons.Rounded.Schedule,
            "Перемена • через ${formatMinutes(state.minutesUntilStart)} ${state.nextLesson.pairNumber} пара",
            buildString {
                append(state.nextLesson.subject)
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
            tint = if (isNeon) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
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

private fun formatMinutes(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h} ч ${m} мин" else "$m мин"
}
