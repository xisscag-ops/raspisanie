package ru.ngieu.raspisanie.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.ngieu.raspisanie.R
import ru.ngieu.raspisanie.ui.theme.AeroPalette
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.PaperPalette
import ru.ngieu.raspisanie.ui.theme.TerminalPalette

/**
 * Полноэкранный фон оформления:
 * - В режиме NEON: неоновое фиолетово-голубое свечение поверх глубокого тёмного фона.
 * - В режиме GLASS: монохромные шёлковые волны (отдельно прикреплённый фон).
 * - В режиме AERO: классические глянцевые обои Windows 7 Aero.
 * - В стандартных темах: ровный фон MaterialTheme.
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
                if (style.isPaper) {
                    val w = size.width
                    val h = size.height
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PaperPalette.surfaceElevated,
                                PaperPalette.backgroundBase,
                            ),
                            center = Offset(w * 0.5f, h * 0.25f),
                            radius = maxOf(w, h) * 0.85f,
                        ),
                    )
                    return@drawBehind
                }
                if (style.isTerminal) {
                    val w = size.width
                    val h = size.height
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                TerminalPalette.vignetteCenter.copy(alpha = 0.85f),
                                TerminalPalette.backgroundBase,
                            ),
                            center = Offset(w * 0.5f, h * 0.42f),
                            radius = maxOf(w, h) * 0.72f,
                        ),
                    )
                    // Тонкие горизонтальные CRT-строки развёртки
                    val step = 3.5.dp.toPx()
                    var y = 0f
                    while (y < h) {
                        drawLine(
                            color = TerminalPalette.scanline,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                        )
                        y += step
                    }
                    return@drawBehind
                }
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
    ) {
        if (style.isGlass) {
            Image(
                painter = painterResource(id = R.drawable.bg_glass_waves),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (style.isAero) {
            Image(
                painter = painterResource(id = R.drawable.bg_aero_wallpaper),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        content()
    }
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
    val cutSize = (cornerRadius * 0.65f).coerceIn(4.dp, 13.dp)
    val shape: Shape = if (style.isTerminal) {
        CutCornerShape(cutSize)
    } else {
        RoundedCornerShape(cornerRadius)
    }

    val defaultFill = when {
        style.isNeon -> Brush.verticalGradient(
            colors = listOf(
                Color(0xAD1E274E),
                Color(0xA1121834),
            ),
        )
        style.isGlass -> Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to GlassPalette.cardFillTop,
                0.48f to GlassPalette.cardFillMid,
                1.0f to GlassPalette.cardFillBottom,
            ),
        )
        style.isAero -> AeroPalette.cardFillBrush()
        style.isTerminal -> TerminalPalette.cardFillBrush()
        style.isPaper -> PaperPalette.cardFillBrush()
        else -> Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
            ),
        )
    }

    val defaultBorder = when {
        style.isNeon -> Brush.linearGradient(
            colors = listOf(
                NeonPalette.glassBorderTop,
                NeonPalette.glassBorderBottom,
            ),
        )
        style.isGlass -> Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to GlassPalette.cardBorderTop,
                0.48f to GlassPalette.cardBorderMid,
                1.0f to GlassPalette.cardBorderBottom,
            ),
        )
        style.isAero -> AeroPalette.glassBorderBrush()
        style.isTerminal -> TerminalPalette.glassBorderBrush()
        style.isPaper -> PaperPalette.borderBrush()
        else -> Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.outlineVariant,
                MaterialTheme.colorScheme.outlineVariant,
            ),
        )
    }

    val glowModifier = if (glowColor != null && style.isTranslucent) {
        Modifier.drawBehind {
            if (style.isTerminal) {
                val c = cutSize.toPx()
                val pad1 = 2.5.dp.toPx()
                drawPath(
                    path = chamferedRectPath(-pad1, -pad1, size.width + pad1 * 2, size.height + pad1 * 2, c + pad1 * 0.45f),
                    color = glowColor.copy(alpha = 0.22f),
                    style = Stroke(width = 3.dp.toPx()),
                )
                val pad2 = 5.dp.toPx()
                drawPath(
                    path = chamferedRectPath(-pad2, -pad2, size.width + pad2 * 2, size.height + pad2 * 2, c + pad2 * 0.45f),
                    color = glowColor.copy(alpha = 0.09f),
                    style = Stroke(width = 4.dp.toPx()),
                )
                return@drawBehind
            }
            val radiusPx = cornerRadius.toPx()
            val outerAlpha = when {
                style.isGlass -> 0.14f
                style.isAero -> 0.22f
                else -> 0.22f
            }
            val haloAlpha = when {
                style.isGlass -> 0.06f
                style.isAero -> 0.10f
                else -> 0.10f
            }
            drawRoundRect(
                color = glowColor.copy(alpha = outerAlpha),
                topLeft = Offset(-3.dp.toPx(), -2.dp.toPx()),
                size = Size(
                    width = size.width + 6.dp.toPx(),
                    height = size.height + 5.dp.toPx(),
                ),
                cornerRadius = CornerRadius(radiusPx + 3.dp.toPx(), radiusPx + 3.dp.toPx()),
            )
            drawRoundRect(
                color = glowColor.copy(alpha = haloAlpha),
                topLeft = Offset(-6.dp.toPx(), -4.dp.toPx()),
                size = Size(
                    width = size.width + 12.dp.toPx(),
                    height = size.height + 10.dp.toPx(),
                ),
                cornerRadius = CornerRadius(radiusPx + 6.dp.toPx(), radiusPx + 6.dp.toPx()),
            )
        }
    } else {
        Modifier
    }

    val innerAccentModifier = when {
        style.isAero -> Modifier.drawWithContent {
            drawContent()
            val inset = 1.8.dp.toPx()
            val r = (cornerRadius - 1.5.dp).coerceAtLeast(0.dp).toPx()
            if (size.width > inset * 2 && size.height > inset * 2) {
                drawRoundRect(
                    color = AeroPalette.innerContour,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 0.85.dp.toPx()),
                )
            }
        }
        style.isTerminal -> Modifier.drawWithContent {
            drawContent()
            val inset = 2.5.dp.toPx()
            val c = (cutSize - 2.dp).coerceAtLeast(2.dp).toPx()
            if (size.width > inset * 2 + c && size.height > inset * 2 + c) {
                drawPath(
                    path = chamferedRectPath(
                        left = inset,
                        top = inset,
                        width = size.width - inset * 2,
                        height = size.height - inset * 2,
                        cut = c,
                    ),
                    color = TerminalPalette.glowGreen.copy(alpha = 0.14f),
                    style = Stroke(width = 0.75.dp.toPx()),
                )
            }
        }
        else -> Modifier
    }

    Box(
        modifier = modifier
            .then(glowModifier)
            .clip(shape)
            .background(brush = fillBrush ?: defaultFill, shape = shape)
            .border(width = borderWidth, brush = borderBrush ?: defaultBorder, shape = shape)
            .then(innerAccentModifier)
            .let { mod -> if (onClick != null) mod.clickable(onClick = onClick) else mod },
        content = content,
    )
}

private fun chamferedRectPath(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    cut: Float,
): Path {
    val c = cut.coerceAtMost(minOf(width, height) * 0.45f)
    val right = left + width
    val bottom = top + height
    return Path().apply {
        moveTo(left + c, top)
        lineTo(right - c, top)
        lineTo(right, top + c)
        lineTo(right, bottom - c)
        lineTo(right - c, bottom)
        lineTo(left + c, bottom)
        lineTo(left, bottom - c)
        lineTo(left, top + c)
        close()
    }
}
