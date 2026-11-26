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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
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
import ru.ngieu.raspisanie.ui.theme.AeroPalette
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.PaperPalette
import ru.ngieu.raspisanie.ui.theme.ScheduleAccentColors
import ru.ngieu.raspisanie.ui.theme.TerminalPalette
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
            cornerRadius = when {
                style.isGlass -> 26.dp
                style.isAero -> 20.dp
                style.isTerminal -> 14.dp
                style.isPaper -> 18.dp
                else -> 24.dp
            },
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
                style.isAero -> AeroPalette.dockFillBrush()
                style.isTerminal -> TerminalPalette.dockFillBrush()
                style.isPaper -> PaperPalette.dockFillBrush()
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
                style.isAero -> AeroPalette.glassBorderBrush()
                style.isTerminal -> TerminalPalette.glassBorderBrush()
                style.isPaper -> PaperPalette.borderBrush()
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
                style.isAero -> Color(0xFF38BDF8)
                style.isTerminal -> TerminalPalette.glowGreen
                else -> null
            },
        ) {
            DaySelectorRow(
                weekDays = weekDays,
                selectedDate = selectedDate,
                todayDate = todayDate,
                lessonsByDay = lessonsByDay,
                onSelectDate = onSelectDate,
                modifier = Modifier.padding(
                    horizontal = if (style.isPaper) 2.dp else 6.dp,
                    vertical = if (style.isPaper) 2.dp else 6.dp,
                ),
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
        horizontalArrangement = Arrangement.spacedBy(if (style.isPaper) 0.dp else 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        weekDays.forEachIndexed { index, date ->
            val isSelected = date == selectedDate
            val isToday = date == todayDate
            val dayLessons = lessonsByDay[date].orEmpty()
            val hasChanges = dayLessons.any { it.isChange }
            val hasCancelled = dayLessons.any { it.isCancelled }
            val isStandardDark = !style.isTranslucent && style.isDark

            val scale = if (isSelected && !style.isPaper) 1.02f else 1f

            val labelColor = when {
                isSelected && style.isTerminal -> TerminalPalette.textPrimary
                isSelected && style.isPaper -> PaperPalette.activeText
                isSelected -> Color.White
                style.isNeon -> NeonPalette.textSecondary
                style.isGlass -> GlassPalette.textMuted
                style.isAero -> AeroPalette.textMuted
                style.isTerminal -> TerminalPalette.textSecondary
                style.isPaper -> PaperPalette.textSecondary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            val numberColor = when {
                isSelected && style.isTerminal -> TerminalPalette.textPrimary
                isSelected && style.isPaper -> PaperPalette.activeText
                isSelected -> if (style.isGlass) Color(0xFFF5F5F5) else Color.White
                isToday && style.isNeon -> Color(0xFF38BDF8)
                isToday && style.isGlass -> Color.White
                isToday && style.isAero -> Color.White
                isToday && style.isTerminal -> TerminalPalette.timelineDot
                isToday && style.isPaper -> PaperPalette.textPrimary
                isToday -> MaterialTheme.colorScheme.primary
                style.isNeon -> NeonPalette.textPrimary
                style.isGlass -> Color(0xFFF5F5F5)
                style.isAero -> AeroPalette.textPrimary
                style.isTerminal -> TerminalPalette.textPrimary
                style.isPaper -> PaperPalette.textPrimary
                else -> MaterialTheme.colorScheme.onSurface
            }

            val tileRadius = when {
                style.isAero -> 13.dp
                style.isTerminal -> 9.dp
                style.isPaper -> 15.dp
                else -> 16.dp
            }
            val shape = if (style.isTerminal) CutCornerShape(tileRadius) else RoundedCornerShape(tileRadius)
            val fillBrush = when {
                isSelected && style.isNeon -> Brush.verticalGradient(
                    colors = listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                )
                isSelected && style.isGlass -> Brush.linearGradient(
                    colors = listOf(GlassPalette.daySelectedFillTop, GlassPalette.daySelectedFillBottom),
                )
                isSelected && style.isAero -> AeroPalette.activeFillBrush()
                isSelected && style.isTerminal -> TerminalPalette.activeFillBrush()
                isSelected && style.isPaper -> PaperPalette.activeFillBrush()
                isSelected -> Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                )
                style.isPaper && isToday -> Brush.verticalGradient(
                    colors = listOf(PaperPalette.surfaceMuted, PaperPalette.surfaceMuted),
                )
                style.isPaper -> Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Transparent),
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
                style.isAero && isToday -> Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xE63D9ED6),
                        0.46f to Color(0xE62282C2),
                        0.50f to Color(0xEB0865A8),
                        1.00f to Color(0xEB157BC0),
                    ),
                )
                style.isAero -> AeroPalette.dayTileFillBrush()
                style.isTerminal && isToday -> Brush.verticalGradient(
                    colors = listOf(Color(0xEB123817), Color(0xEE081E0B)),
                )
                style.isTerminal -> TerminalPalette.dayTileFillBrush()
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
                isSelected && style.isAero -> AeroPalette.activeBorderBrush()
                isSelected && style.isTerminal -> TerminalPalette.activeBorderBrush()
                isToday && style.isNeon -> Brush.verticalGradient(
                    colors = listOf(Color(0x8038BDF8), Color(0x40818CF8)),
                )
                isToday && style.isGlass -> Brush.linearGradient(
                    colors = listOf(Color(0x99F4F4F4), Color(0x408C8C8C)),
                )
                isToday && style.isAero -> Brush.verticalGradient(
                    colors = listOf(Color(0xF0D6F5FF), Color(0xAA78C8F0)),
                )
                isToday && style.isTerminal -> Brush.verticalGradient(
                    colors = listOf(TerminalPalette.activeBorderTop, TerminalPalette.borderBottom),
                )
                style.isNeon -> Brush.verticalGradient(
                    colors = listOf(Color(0x28FFFFFF), Color(0x14818CF8)),
                )
                style.isGlass -> Brush.linearGradient(
                    colors = listOf(GlassPalette.dayTileBorderTop, GlassPalette.dayTileBorderBottom),
                )
                style.isAero -> Brush.verticalGradient(
                    colors = listOf(Color(0xA6BCEFFE), Color(0x4D3D8EC2)),
                )
                style.isTerminal -> TerminalPalette.glassBorderBrush()
                style.isPaper && isSelected -> PaperPalette.activeFillBrush()
                style.isPaper -> Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Transparent),
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
                        if (isSelected && style.isTranslucent && !style.isTerminal) {
                            val r = tileRadius.toPx()
                            val glow = when {
                                style.isGlass -> Color.White.copy(alpha = 0.28f)
                                style.isAero -> Color(0xFF7DD3FC).copy(alpha = 0.36f)
                                else -> Color(0xFF9333EA).copy(alpha = 0.35f)
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
                                isSelected && style.isTerminal -> TerminalPalette.timelineDot
                                isSelected && style.isPaper -> PaperPalette.activeText
                                isSelected -> Color.White
                                hasCancelled -> if (isStandardDark) ScheduleAccentColors.cancelBadgeDark else Color(0xFFF87171)
                                hasChanges -> if (isStandardDark) ScheduleAccentColors.changeBadgeDark else Color(0xFFFBBF24)
                                style.isGlass -> Color(0xFFFAFAFA)
                                style.isAero -> Color(0xFFEAF8FF)
                                style.isTerminal -> TerminalPalette.timelineDot
                                style.isPaper -> PaperPalette.textPrimary
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
            if (style.isPaper && index < weekDays.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(48.dp)
                        .background(PaperPalette.divider),
                )
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
