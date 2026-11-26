package ru.ngieu.raspisanie.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.ngieu.raspisanie.domain.WeekFilterMode
import ru.ngieu.raspisanie.ui.theme.AeroPalette
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val russianLocale = Locale("ru", "RU")
private val shortDayMonthFormatter = DateTimeFormatter.ofPattern("d MMM", russianLocale)

@Composable
fun WeekSelectorBar(
    selectedMonday: LocalDate,
    activeWeekFilter: WeekFilterMode,
    detectedWeekForSelectedMonday: WeekFilterMode?,
    showTodayButton: Boolean,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onGoToday: () -> Unit,
    onSelectWeekMode: (WeekFilterMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = LocalAppVisualStyle.current
    val headerTextColor = when {
        style.isNeon -> NeonPalette.textPrimary
        style.isGlass -> GlassPalette.textPrimary
        style.isAero -> AeroPalette.textPrimary
        else -> MaterialTheme.colorScheme.onBackground
    }
    val inactiveSubColor = when {
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        style.isAero -> AeroPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val saturday = selectedMonday.plusDays(5)
    val rangeText = "${selectedMonday.format(shortDayMonthFormatter)} – ${saturday.format(shortDayMonthFormatter)}"

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onPreviousWeek,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Предыдущая неделя",
                        tint = headerTextColor,
                    )
                }
                AnimatedContent(
                    targetState = rangeText,
                    transitionSpec = {
                        (slideInVertically(tween(110)) { it / 3 } + fadeIn(tween(110)))
                            .togetherWith(slideOutVertically(tween(90)) { -it / 3 } + fadeOut(tween(90)))
                    },
                    label = "weekRangeText",
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = headerTextColor,
                    )
                }
                IconButton(
                    onClick = onNextWeek,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Следующая неделя",
                        tint = headerTextColor,
                    )
                }
            }

            AnimatedVisibility(
                visible = showTodayButton,
                enter = scaleIn(tween(110)) + fadeIn(tween(110)),
                exit = scaleOut(tween(90)) + fadeOut(tween(90)),
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        style.isNeon -> Color(0x409333EA)
                        style.isGlass -> GlassPalette.typePillBg
                        style.isAero -> AeroPalette.activeFillMidUpper
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = when {
                        style.isNeon -> Color(0xFFE9D5FF)
                        style.isGlass -> Color.White
                        style.isAero -> Color.White
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onGoToday),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Today,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "Сегодня",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WeekFilterMode.entries.forEach { mode ->
                val selected = activeWeekFilter == mode
                val isActualWeek = detectedWeekForSelectedMonday == mode
                val fillBrush = when {
                    selected && style.isNeon -> Brush.verticalGradient(
                        listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                    )
                    selected && style.isGlass -> Brush.linearGradient(
                        listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                    )
                    selected && style.isAero -> AeroPalette.activeFillBrush()
                    selected -> Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                    )
                    else -> null
                }
                val borderBrush = when {
                    selected && style.isNeon -> Brush.linearGradient(
                        listOf(Color(0x99E9D5FF), Color(0x667DD3FC)),
                    )
                    selected && style.isGlass -> Brush.linearGradient(
                        listOf(GlassPalette.panelBorderTop, GlassPalette.panelBorderBottom),
                    )
                    selected && style.isAero -> AeroPalette.activeBorderBrush()
                    selected -> Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                    )
                    style.isNeon -> Brush.linearGradient(
                        listOf(NeonPalette.glassBorderTop, NeonPalette.glassBorderBottom),
                    )
                    style.isGlass -> Brush.linearGradient(
                        listOf(GlassPalette.cardBorderTop, GlassPalette.cardBorderBottom),
                    )
                    style.isAero -> AeroPalette.glassBorderBrush()
                    else -> Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.colorScheme.outlineVariant),
                    )
                }
                val textColor = when {
                    selected && style.isTranslucent -> Color.White
                    selected -> MaterialTheme.colorScheme.onPrimary
                    else -> inactiveSubColor
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 12.dp,
                    fillBrush = fillBrush,
                    borderBrush = borderBrush,
                    borderWidth = if (selected) 1.3.dp else 1.dp,
                    onClick = { onSelectWeekMode(mode) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${mode.title} неделя",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                        )
                        if (isActualWeek) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    selected -> textColor
                                    style.isGlass || style.isAero -> Color.White
                                    else -> Color(0xFF38BDF8)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
