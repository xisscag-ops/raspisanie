package ru.ngieu.raspisanie.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ngieu.raspisanie.R
import ru.ngieu.raspisanie.ui.MainNavTab
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette

private data class NavTabItem(
    val tab: MainNavTab,
    val label: String,
    @DrawableRes val iconRes: Int,
)

private val navItems = listOf(
    NavTabItem(MainNavTab.HOME, "Главная", R.drawable.ic_neon_home),
    NavTabItem(MainNavTab.WEEK, "Неделя", R.drawable.ic_neon_calendar),
    NavTabItem(MainNavTab.GROUP, "Звонки", R.drawable.ic_neon_group),
    NavTabItem(MainNavTab.SETTINGS, "Настройки", R.drawable.ic_neon_settings_nav),
)

@Composable
fun NeonTopNavBar(
    activeTab: MainNavTab,
    onSelectTab: (MainNavTab) -> Unit,
    hasAppUpdate: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val style = LocalAppVisualStyle.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        GlassCard(
            cornerRadius = 22.dp,
            fillBrush = if (style.isNeon) {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xCC1A1F44),
                        Color(0xBF131936),
                        Color(0xCC15224B),
                    ),
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            borderBrush = if (style.isNeon) {
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x55A855F7),
                        Color(0x28FFFFFF),
                        Color(0x5538BDF8),
                    ),
                )
            } else {
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outlineVariant,
                        MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navItems.forEach { item ->
                    val selected = item.tab == activeTab
                    val labelColor by animateColorAsState(
                        targetValue = when {
                            selected && style.isNeon -> Color.White
                            selected -> MaterialTheme.colorScheme.onPrimary
                            style.isNeon -> NeonPalette.textSecondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = tween(90),
                        label = "navLabel",
                    )

                    val pillShape = RoundedCornerShape(17.dp)
                    val activeBrush = if (style.isNeon) {
                        Brush.verticalGradient(
                            colors = listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .drawBehind {
                                if (selected && style.isNeon) {
                                    val r = 17.dp.toPx()
                                    drawRoundRect(
                                        color = Color(0xFF9333EA).copy(alpha = 0.36f),
                                        topLeft = Offset(-1.5.dp.toPx(), -1.5.dp.toPx()),
                                        size = Size(
                                            width = size.width + 3.dp.toPx(),
                                            height = size.height + 3.dp.toPx(),
                                        ),
                                        cornerRadius = CornerRadius(r + 1.5.dp.toPx(), r + 1.5.dp.toPx()),
                                    )
                                }
                            }
                            .clip(pillShape)
                            .then(
                                if (selected) {
                                    Modifier
                                        .background(brush = activeBrush, shape = pillShape)
                                        .border(
                                            width = 1.dp,
                                            color = if (style.isNeon) Color(0x88E9D5FF) else MaterialTheme.colorScheme.primary,
                                            shape = pillShape,
                                        )
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { onSelectTab(item.tab) }
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box {
                                Image(
                                    painter = painterResource(id = item.iconRes),
                                    contentDescription = item.label,
                                    colorFilter = when {
                                        selected && style.isNeon -> null
                                        selected -> ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                                        style.isNeon -> ColorFilter.tint(NeonPalette.textSecondary)
                                        else -> ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                                    },
                                    modifier = Modifier.size(19.dp),
                                )
                                if (hasAppUpdate && item.tab == MainNavTab.SETTINGS) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(7.dp)
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(NeonPalette.timelineActiveNow),
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = labelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}
