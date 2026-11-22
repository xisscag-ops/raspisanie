package ru.ngieu.raspisanie.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.ngieu.raspisanie.data.Lesson
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun DaySelectorRow(
    weekDays: List<LocalDate>,
    selectedDate: LocalDate,
    todayDate: LocalDate,
    lessonsByDay: Map<LocalDate, List<Lesson>>,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalAppVisualStyle.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        weekDays.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == todayDate
            val dayLessons = lessonsByDay[date].orEmpty()
            val hasChanges = dayLessons.any { it.isChange }

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.03f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "dayScale",
            )

            val labelColor by animateColorAsState(
                targetValue = when {
                    isSelected -> Color.White
                    style.isNeon -> NeonPalette.textSecondary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = tween(200),
                label = "dayLabelColor",
            )

            val numberColor by animateColorAsState(
                targetValue = when {
                    isSelected -> Color.White
                    isToday && style.isNeon -> Color(0xFF38BDF8)
                    isToday -> MaterialTheme.colorScheme.primary
                    style.isNeon -> NeonPalette.textPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                animationSpec = tween(200),
                label = "dayNumberColor",
            )

            val shape = RoundedCornerShape(16.dp)
            val fillBrush = when {
                isSelected && style.isNeon -> Brush.verticalGradient(
                    colors = listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                )
                isSelected -> Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                )
                style.isNeon && isToday -> Brush.verticalGradient(
                    colors = listOf(Color(0xB8212A56), Color(0xA6141B3A)),
                )
                style.isNeon -> Brush.verticalGradient(
                    colors = listOf(Color(0x9E192044), Color(0x99111632)),
                )
                isToday -> Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    ),
                )
                else -> Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface),
                )
            }

            val borderBrush = when {
                isSelected && style.isNeon -> Brush.verticalGradient(
                    colors = listOf(NeonPalette.activeDayBorder, Color(0xFFA855F7)),
                )
                isToday && style.isNeon -> Brush.verticalGradient(
                    colors = listOf(Color(0x8038BDF8), Color(0x40818CF8)),
                )
                style.isNeon -> Brush.verticalGradient(
                    colors = listOf(NeonPalette.glassBorderTop, NeonPalette.glassBorderBottom),
                )
                isSelected -> Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                )
                isToday -> Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    ),
                )
                else -> Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant),
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBehind {
                        if (isSelected && style.isNeon) {
                            val r = 16.dp.toPx()
                            drawRoundRect(
                                color = Color(0xFF9333EA).copy(alpha = 0.35f),
                                topLeft = Offset(-3.dp.toPx(), -2.dp.toPx()),
                                size = Size(
                                    width = size.width + 6.dp.toPx(),
                                    height = size.height + 6.dp.toPx(),
                                ),
                                cornerRadius = CornerRadius(r + 3.dp.toPx(), r + 3.dp.toPx()),
                            )
                        }
                    }
                    .clip(shape)
                    .background(brush = fillBrush, shape = shape)
                    .border(width = if (isSelected) 1.3.dp else 1.dp, brush = borderBrush, shape = shape)
                    .clickable { onSelectDate(date) },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = shortDayName(date.dayOfWeek),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = labelColor,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.SemiBold,
                        color = numberColor,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Box(
                        modifier = Modifier.height(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = dayLessons.isNotEmpty(),
                            enter = scaleIn(tween(180)) + fadeIn(tween(180)),
                            exit = scaleOut(tween(150)) + fadeOut(tween(150)),
                        ) {
                            val dotColor = when {
                                isSelected -> Color.White
                                hasChanges -> Color(0xFFFBBF24)
                                style.isNeon && date.dayOfWeek.value >= 5 -> Color(0xFF38BDF8)
                                style.isNeon -> Color(0xFFA78BFA)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(dotColor),
                            )
                        }
                    }
                }
            }
        }
    }
}

fun shortDayName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "Пн"
    DayOfWeek.TUESDAY -> "Вт"
    DayOfWeek.WEDNESDAY -> "Ср"
    DayOfWeek.THURSDAY -> "Чт"
    DayOfWeek.FRIDAY -> "Пт"
    DayOfWeek.SATURDAY -> "Сб"
    DayOfWeek.SUNDAY -> "Вс"
}

fun fullDayName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "Понедельник"
    DayOfWeek.TUESDAY -> "Вторник"
    DayOfWeek.WEDNESDAY -> "Среда"
    DayOfWeek.THURSDAY -> "Четверг"
    DayOfWeek.FRIDAY -> "Пятница"
    DayOfWeek.SATURDAY -> "Суббота"
    DayOfWeek.SUNDAY -> "Воскресенье"
}
