package ru.ngieu.raspisanie.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ru.ngieu.raspisanie.data.WeekParity
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonCard(
    lesson: Lesson,
    isCurrentNow: Boolean,
    themeMode: ThemeModeSetting,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val style = LocalAppVisualStyle.current
    val isDark = style.isDark
    val typeColor = ScheduleAccentColors.lessonTypeColor(lesson.lessonType, isDark)

    val timelineDotColor by animateColorAsState(
        targetValue = when {
            lesson.isChange -> Color(0xFFFBBF24)
            isCurrentNow -> NeonPalette.timelineCyan
            style.isNeon -> NeonPalette.timelinePurple
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(220),
        label = "timelineDot",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Левая ось таймлайна
        Canvas(
            modifier = Modifier
                .width(18.dp)
                .fillMaxHeight(),
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val lineColor = if (style.isNeon) {
                Color(0xFF7C3AED).copy(alpha = 0.72f)
            } else {
                Color(0xFF94A3B8).copy(alpha = 0.45f)
            }

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

            // Мягкое свечение точки на таймлайне
            drawCircle(
                color = timelineDotColor.copy(alpha = if (isCurrentNow) 0.48f else 0.28f),
                radius = if (isCurrentNow) 9.dp.toPx() else 7.dp.toPx(),
                center = Offset(centerX, centerY),
            )
            drawCircle(
                color = timelineDotColor,
                radius = 5.dp.toPx(),
                center = Offset(centerX, centerY),
            )
            drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = Offset(centerX, centerY),
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Колонка времени начала и конца пары
        val timeParts = lesson.timeRangeText.split("–").map { it.trim() }
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
                    color = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeParts[1],
                    style = MaterialTheme.typography.bodySmall,
                    color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = lesson.timeRangeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Правая стеклянная карточка пары
        val cardFillBrush = when {
            lesson.isChange && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(Color(0xBD3D260A), Color(0xB3271907)),
            )
            lesson.isChange -> Brush.horizontalGradient(
                colors = listOf(
                    if (isDark) ScheduleAccentColors.changeBgDark else ScheduleAccentColors.changeBgLight,
                    if (isDark) ScheduleAccentColors.changeBgDark else ScheduleAccentColors.changeBgLight,
                ),
            )
            isCurrentNow && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(NeonPalette.activeLessonStart, NeonPalette.activeLessonEnd),
            )
            isCurrentNow -> Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                ),
            )
            style.isNeon -> Brush.verticalGradient(
                colors = listOf(Color(0xAE1D254B), Color(0x9E131935)),
            )
            else -> Brush.verticalGradient(
                colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface),
            )
        }

        val cardBorderBrush = when {
            lesson.isChange -> Brush.horizontalGradient(
                colors = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B)),
            )
            isCurrentNow && style.isNeon -> Brush.horizontalGradient(
                colors = listOf(Color(0xFF60A5FA), NeonPalette.activeLessonBorder),
            )
            isCurrentNow -> Brush.horizontalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
            )
            style.isNeon -> Brush.linearGradient(
                colors = listOf(NeonPalette.glassBorderTop, NeonPalette.glassBorderBottom),
            )
            else -> Brush.linearGradient(
                colors = listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant),
            )
        }

        val glowColor = when {
            !style.isNeon -> null
            lesson.isChange -> Color(0xFFF59E0B)
            isCurrentNow -> NeonPalette.activeLessonBorder
            else -> null
        }

        GlassCard(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 5.dp),
            cornerRadius = 18.dp,
            fillBrush = cardFillBrush,
            borderBrush = cardBorderBrush,
            borderWidth = if (isCurrentNow || lesson.isChange) 1.5.dp else 1.dp,
            glowColor = glowColor,
            onClick = onClick,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (style.isNeon) typeColor else typeColor.copy(alpha = 0.14f),
                                )
                                .padding(horizontal = 9.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = lesson.lessonType.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (style.isNeon) Color.White else typeColor,
                            )
                        }

                        if (lesson.isChange) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF59E0B).copy(alpha = if (style.isNeon) 0.25f else 0.16f),
                                contentColor = Color(0xFFFBBF24),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = null,
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

                        if (isCurrentNow) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0EA5E9).copy(alpha = 0.26f))
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = "СЕЙЧАС",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7DD3FC),
                                )
                            }
                        }

                        if (lesson.parity != WeekParity.EVERY) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (style.isNeon) Color(0x22FFFFFF) else MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                            ) {
                                Text(
                                    text = lesson.parity.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = lesson.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (style.isNeon) NeonPalette.textPrimary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val metaParts = buildList {
                        if (lesson.room.isNotBlank()) add("Ауд. ${lesson.room}")
                        if (lesson.teachers.isNotEmpty()) add(lesson.teachers.joinToString(", "))
                    }

                    if (metaParts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            val iconRes = if (lesson.room.isNotBlank() && lesson.teachers.isEmpty()) {
                                R.drawable.ic_neon_room
                            } else if (lesson.room.isNotBlank()) {
                                R.drawable.ic_neon_room
                            } else {
                                R.drawable.ic_neon_teacher
                            }
                            Image(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                colorFilter = if (style.isNeon) null else ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = metaParts.joinToString(" • "),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (style.isNeon) NeonPalette.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_neon_chevron_right),
                    contentDescription = "Подробнее",
                    colorFilter = if (style.isNeon) null else ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
