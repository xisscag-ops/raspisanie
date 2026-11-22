package ru.ngieu.raspisanie.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.ngieu.raspisanie.domain.LiveLessonState

@Composable
fun LiveLessonBanner(
    state: LiveLessonState,
    modifier: Modifier = Modifier,
) {
    if (state is LiveLessonState.NoLessonsToday) return

    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(220)),
        label = "liveBanner",
    ) { currentState ->
        when (currentState) {
            is LiveLessonState.InProgress -> {
                val animatedProgress by animateFloatAsState(
                    targetValue = currentState.progress.coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 450),
                    label = "lessonProgress",
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Сейчас идёт ${currentState.currentLesson.pairNumber} пара",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(
                                text = "Осталось ${currentState.minutesRemaining} мин",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = buildString {
                                append(currentState.currentLesson.subject)
                                if (currentState.currentLesson.room.isNotBlank()) {
                                    append(" • ауд. ${currentState.currentLesson.room}")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                        )
                    }
                }
            }

            is LiveLessonState.BeforeFirstLesson -> {
                CompactBanner(
                    icon = Icons.Rounded.Schedule,
                    title = "До первой пары ${formatMinutes(currentState.minutesUntilStart)}",
                    subtitle = buildString {
                        append(currentState.nextLesson.subject)
                        if (currentState.nextLesson.room.isNotBlank()) {
                            append(" • ауд. ${currentState.nextLesson.room}")
                        }
                        append(" (${currentState.nextLesson.timeRangeText})")
                    },
                )
            }

            is LiveLessonState.OnBreak -> {
                CompactBanner(
                    icon = Icons.Rounded.Schedule,
                    title = "Перемена • через ${formatMinutes(currentState.minutesUntilStart)} ${currentState.nextLesson.pairNumber} пара",
                    subtitle = buildString {
                        append(currentState.nextLesson.subject)
                        if (currentState.nextLesson.room.isNotBlank()) {
                            append(" • ауд. ${currentState.nextLesson.room}")
                        }
                    },
                )
            }

            is LiveLessonState.FinishedForToday -> {
                CompactBanner(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Пары на сегодня закончились",
                    subtitle = "Можно отдохнуть или посмотреть расписание на завтра",
                )
            }

            LiveLessonState.NoLessonsToday -> Unit
        }
    }
}

@Composable
private fun CompactBanner(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun formatMinutes(minutes: Long): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h} ч ${m} мин" else "$m мин"
}
