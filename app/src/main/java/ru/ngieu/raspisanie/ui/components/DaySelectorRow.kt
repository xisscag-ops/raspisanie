package ru.ngieu.raspisanie.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun DaySelectorBottomDock(
    weekDays: List<LocalDate>,
    selectedDate: LocalDate,
    todayDate: LocalDate,
    lessonsByDay: Map<LocalDate, List<Lesson>>,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalAppVisualStyle.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        GlassCard(
            cornerRadius = if (style.isGlass) 26.dp else 24.dp,
            fillBrush = when {
                style.isNeon -> Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xE01A1F44),
                        Color(0xD9131936),
                        Color(0xE015224B),
                    ),
                )
                style.isGlass -> Brush.verticalGradient(
                    colors = listOf(
                        GlassPalette.dockFillTop,
                        GlassPalette.dockFillBottom,
                    ),
                )
                else -> Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            borderBrush = when {
                style.isNeon -> Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x66A855F7),
                        Color(0x33FFFFFF),
                        Color(0x6638BDF8),
                    ),
                )
                style.isGlass -> Brush.verticalGradient(
                    colors = listOf(
                        GlassPalette.dockBorderTop,
                        GlassPalette.dockBorderBottom,
                    ),
                )
                else -> Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            },
            glowColor = when {
                style.isNeon -> Color(0xFF7C3AED)
                style.isGlass -> Color.White
                else -> null
            },
        ) {
            DaySelectorRow(
                weekDays = weekDays,
                selectedDate = selectedDate,
                todayDate = todayDate,
                lessonsByDay = lessonsByDay,
                onSelectDate = onSelectDate,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            )
        }
    }
}

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
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        weekDays.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == todayDate
            val dayLessons = lessonsByDay[date].orEmpty()
            val hasChanges = dayLessons.any { it.isChange }

            val scale = if (isSelected) 1.02f else 1f

            val labelColor = when {
                isSelected -> Color.White
                style.isNeon -> NeonPalette.textSecondary
                style.isGlass -> GlassPalette.textMuted
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            val numberColor = when {
                isSelected -> if (style.isGlass) Color(0xFFF5F5F5) else Color.White
                isToday && style.isNeon -> Color(0xFF38BDF8)
                isToday && style.isGlass -> Color.White
                isToday -> MaterialTheme.colorScheme.primary
                style.isNeon -> NeonPalette.textPrimary
                style.isGlass -> Color(0xFFF5F5F5)
                else -> MaterialTheme.colorScheme.onSurface
            }

            val shape = RoundedCornerShape(16.dp)
            val fillBrush = when {
                isSelected && style.isNeon -> Brush.verticalGradient(
                    colors = listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                )
                isSelected && style.isGlass -> Brush.linearGradient(
                    colors = listOf(GlassPalette.daySelectedFillTop, GlassPalette.daySelectedFillBottom),
                )
                isSelected -> Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                )
                style.isNeon && isToday -> Brush.verticalGradient(
                    colors = listOf(Color(0xB8212A56), Color(0xA6141B3A)),
                )
                style.isNeon -> Brush.verticalGradient(
                    colors = listOf(Color(0x88192044), Color(0x80111632)),
                )
                style.isGlass && isToday -> Brush.linearGradient(
                    colors = listOf(Color(0xB3686868), Color(0xA6363636)),
                )
                style.isGlass -> Brush.linearGradient(
                    colors = listOf(GlassPalette.dayTileFillTop, GlassPalette.dayTileFillBottom),
                )
                isToday -> Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    ),
                )
                else -> Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    ),
                )
            }

            val borderBrush = when {
                isSelected && style.isNeon -> Brush.verticalGradient(
                    colors = listOf(NeonPalette.activeDayBorder, Color(0xFFA855F7)),
                )
                isSelected && style.isGlass -> Brush.linearGradient(
                    colors = listOf(GlassPalette.daySelectedBorderTop, GlassPalette.daySelectedBorderBottom),
                )
                isToday && style.isNeon -> Brush.verticalGradient(
                    colors = listOf(Color(0x8038BDF8), Color(0x40818CF8)),
                )
                isToday && style.isGlass -> Brush.linearGradient(
                    colors = listOf(Color(0x99F4F4F4), Color(0x408C8C8C)),
                )
                style.isNeon -> Brush.verticalGradient(
                    colors = listOf(Color(0x28FFFFFF), Color(0x14818CF8)),
                )
                style.isGlass -> Brush.linearGradient(
                    colors = listOf(GlassPalette.dayTileBorderTop, GlassPalette.dayTileBorderBottom),
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
                        if (isSelected && style.isTranslucent) {
                            val r = 16.dp.toPx()
                            val glow = if (style.isGlass) {
                                Color.White.copy(alpha = 0.28f)
                            } else {
                                Color(0xFF9333EA).copy(alpha = 0.35f)
                            }
                            drawRoundRect(
                                color = glow,
                                topLeft = Offset(-2.dp.toPx(), -2.dp.toPx()),
                                size = Size(
                                    width = size.width + 4.dp.toPx(),
                                    height = size.height + 4.dp.toPx(),
                                ),
                                cornerRadius = CornerRadius(r + 2.dp.toPx(), r + 2.dp.toPx()),
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
                    modifier = Modifier.padding(vertical = 8.dp),
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.height(5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = dayLessons.isNotEmpty(),
                            enter = scaleIn(tween(90)) + fadeIn(tween(90)),
                            exit = scaleOut(tween(75)) + fadeOut(tween(75)),
                        ) {
                            val dotColor = when {
                                isSelected -> Color.White
                                hasChanges -> if (style.isDark) ScheduleAccentColors.changeBadgeDark else Color(0xFFD97706)
                                style.isGlass -> Color(0xFFFAFAFA)
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
