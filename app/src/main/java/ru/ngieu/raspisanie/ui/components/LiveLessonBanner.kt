package ru.ngieu.raspisanie.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    when (state) {
        is LiveLessonState.InProgress -> {
            Surface(
                modifier = modifier.fillMaxWidth(),
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
                                text = "Сейчас идёт ${state.currentLesson.pairNumber} пара",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Text(
                            text = "Осталось ${state.minutesRemaining} мин",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = buildString {
                            append(state.currentLesson.subject)
                            if (state.currentLesson.room.isNotBlank()) {
                                append(" • ауд. ${state.currentLesson.room}")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.progress.coerceIn(0f, 1f) },
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
                modifier = modifier,
                icon = Icons.Rounded.Schedule,
                title = "До первой пары ${formatMinutes(state.minutesUntilStart)}",
                subtitle = buildString {
                    append(state.nextLesson.subject)
                    if (state.nextLesson.room.isNotBlank()) {
                        append(" • ауд. ${state.nextLesson.room}")
                    }
                    append(" (${state.nextLesson.timeRangeText})")
                },
            )
        }

        is LiveLessonState.OnBreak -> {
            CompactBanner(
                modifier = modifier,
                icon = Icons.Rounded.Schedule,
                title = "Перемена • через ${formatMinutes(state.minutesUntilStart)} ${state.nextLesson.pairNumber} пара",
                subtitle = buildString {
                    append(state.nextLesson.subject)
                    if (state.nextLesson.room.isNotBlank()) {
                        append(" • ауд. ${state.nextLesson.room}")
                    }
                },
            )
        }

        is LiveLessonState.FinishedForToday -> {
            CompactBanner(
                modifier = modifier,
                icon = Icons.Rounded.CheckCircle,
                title = "Пары на сегодня закончились",
                subtitle = "Можно отдохнуть или посмотреть расписание на завтра",
            )
        }

        LiveLessonState.NoLessonsToday -> Unit
    }
}

@Composable
private fun CompactBanner(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
