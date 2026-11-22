package ru.ngieu.raspisanie.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette

/**
 * Полноэкранный неоновый фон с боковым фиолетовым и синим свечением,
 * адаптирующийся под любые пропорции Android-экранов без чёрных углов.
 */
@Composable
fun NeonAuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val baseBg = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
            .drawBehind {
                if (!style.isNeon) return@drawBehind
                val w = size.width
                val h = size.height

                // Левое фиолетовое свечение вдоль края
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonPalette.leftEdgeGlow.copy(alpha = 0.28f),
                            Color(0xFF4C1D95).copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        startX = 0f,
                        endX = w * 0.42f,
                    ),
                )

                // Правое неоново-голубое свечение вдоль края
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeonPalette.rightEdgeGlow.copy(alpha = 0.10f),
                            NeonPalette.rightCyanGlow.copy(alpha = 0.28f),
                        ),
                        startX = w * 0.56f,
                        endX = w,
                    ),
                )

                // Мягкое радиальное пятно слева вверху
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9333EA).copy(alpha = 0.24f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.06f, h * 0.16f),
                        radius = w * 0.65f,
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.06f, h * 0.16f),
                )

                // Мягкое радиальное пятно справа в середине
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0EA5E9).copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.94f, h * 0.48f),
                        radius = w * 0.65f,
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.94f, h * 0.48f),
                )

                // Нижнее акцентированное свечение за навигацией
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7C3AED).copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.22f, h * 0.92f),
                        radius = w * 0.55f,
                    ),
                    radius = w * 0.55f,
                    center = Offset(w * 0.22f, h * 0.92f),
                )
            },
        content = content,
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    fillBrush: Brush? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    glowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val shape: Shape = RoundedCornerShape(cornerRadius)

    val defaultFill = if (style.isNeon) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xAD1E274E),
                Color(0xA1121834),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
            ),
        )
    }

    val defaultBorder = if (style.isNeon) {
        Brush.linearGradient(
            colors = listOf(
                NeonPalette.glassBorderTop,
                NeonPalette.glassBorderBottom,
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.outlineVariant,
                MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }

    val glowModifier = if (glowColor != null && style.isNeon) {
        Modifier.drawBehind {
            val radiusPx = cornerRadius.toPx()
            drawRoundRect(
                color = glowColor.copy(alpha = 0.22f),
                topLeft = Offset(-3.dp.toPx(), -2.dp.toPx()),
                size = size.copy(
                    width = size.width + 6.dp.toPx(),
                    height = size.height + 5.dp.toPx(),
                ),
                cornerRadius = CornerRadius(radiusPx + 3.dp.toPx(), radiusPx + 3.dp.toPx()),
            )
            drawRoundRect(
                color = glowColor.copy(alpha = 0.10f),
                topLeft = Offset(-6.dp.toPx(), -4.dp.toPx()),
                size = size.copy(
                    width = size.width + 12.dp.toPx(),
                    height = size.height + 10.dp.toPx(),
                ),
                cornerRadius = CornerRadius(radiusPx + 6.dp.toPx(), radiusPx + 6.dp.toPx()),
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(glowModifier)
            .clip(shape)
            .background(brush = fillBrush ?: defaultFill, shape = shape)
            .border(width = borderWidth, brush = borderBrush ?: defaultBorder, shape = shape)
            .let { mod -> if (onClick != null) mod.clickable(onClick = onClick) else mod },
        content = content,
    )
}
