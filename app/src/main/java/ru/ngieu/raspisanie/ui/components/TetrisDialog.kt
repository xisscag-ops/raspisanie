package ru.ngieu.raspisanie.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowDown
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import ru.ngieu.raspisanie.domain.LiveLessonState
import ru.ngieu.raspisanie.domain.TetrisEngine
import ru.ngieu.raspisanie.domain.TetrisGameState
import ru.ngieu.raspisanie.domain.TetrominoType
import ru.ngieu.raspisanie.ui.theme.AeroPalette
import ru.ngieu.raspisanie.ui.theme.GlassPalette
import ru.ngieu.raspisanie.ui.theme.LocalAppVisualStyle
import ru.ngieu.raspisanie.ui.theme.NeonPalette
import ru.ngieu.raspisanie.ui.theme.TerminalPalette
import kotlin.math.abs

@Composable
fun TetrisGameDialog(
    initialBestScore: Int,
    liveLessonState: LiveLessonState,
    onUpdateBestScore: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val titleColor = when {
        style.isNeon -> NeonPalette.textPrimary
        style.isGlass -> GlassPalette.textPrimary
        style.isAero -> AeroPalette.textPrimary
        style.isTerminal -> TerminalPalette.textPrimary
        else -> MaterialTheme.colorScheme.onBackground
    }
    val subColor = when {
        style.isNeon -> NeonPalette.textSecondary
        style.isGlass -> GlassPalette.textSecondary
        style.isAero -> AeroPalette.textSecondary
        style.isTerminal -> TerminalPalette.textSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val bag = remember { ArrayDeque<TetrominoType>() }
    var gameState by remember {
        mutableStateOf(TetrisEngine.newGame(bestScore = initialBestScore, bag = bag))
    }

    // Сохраняем лучший результат при каждом изменении рекорда и при закрытии диалога
    LaunchedEffect(gameState.bestScore) {
        if (gameState.bestScore > initialBestScore) {
            onUpdateBestScore(gameState.bestScore)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            onUpdateBestScore(gameState.bestScore)
        }
    }

    // Основной игровой цикл падения фигур
    LaunchedEffect(gameState.isPaused, gameState.isGameOver, gameState.level) {
        if (gameState.isPaused || gameState.isGameOver) return@LaunchedEffect
        while (isActive) {
            delay(gameState.dropIntervalMs)
            gameState = TetrisEngine.stepDown(gameState, bag)
        }
    }

    val breakBadgeText = when (liveLessonState) {
        is LiveLessonState.OnBreak -> "Перемена • до пары ${liveLessonState.minutesUntilStart} мин"
        is LiveLessonState.BeforeFirstLesson -> "До 1 пары ${liveLessonState.minutesUntilStart} мин"
        is LiveLessonState.InProgress -> "Идёт ${liveLessonState.currentLesson.pairNumber} пара • ост. ${liveLessonState.minutesRemaining} мин"
        LiveLessonState.FinishedForToday -> "Пары закончились • можно отдохнуть"
        LiveLessonState.NoLessonsToday -> "Мини-игра для перерывов"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        NeonAuroraBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Верхняя панель игры
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                style.isGlass -> "Стеклянный Тетрис"
                                style.isAero -> "Аэро Тетрис"
                                style.isTerminal -> "Терминал Тетрис"
                                else -> "Неоновый Тетрис"
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = titleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = breakBadgeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                style.isNeon -> Color(0xFF7DD3FC)
                                style.isGlass -> GlassPalette.textSecondary
                                style.isAero -> AeroPalette.textSecondary
                                style.isTerminal -> TerminalPalette.textSecondary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    TetrisIconCircleButton(
                        icon = if (gameState.isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        contentDescription = if (gameState.isPaused) "Продолжить" else "Пауза",
                        enabled = !gameState.isGameOver,
                        onClick = {
                            gameState = gameState.copy(isPaused = !gameState.isPaused)
                        },
                    )
                    TetrisIconCircleButton(
                        icon = Icons.Rounded.Refresh,
                        contentDescription = "Начать заново",
                        onClick = {
                            bag.clear()
                            gameState = TetrisEngine.newGame(bestScore = gameState.bestScore, bag = bag)
                        },
                    )
                    TetrisIconCircleButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Закрыть игру",
                        onClick = onDismiss,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Панель статистики и следующей фигуры
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 16.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TetrisStatItem(label = "Счёт", value = gameState.score.toString(), titleColor = titleColor, subColor = subColor)
                        TetrisStatItem(label = "Рекорд", value = gameState.bestScore.toString(), titleColor = Color(0xFF34D399), subColor = subColor)
                        TetrisStatItem(label = "Линии", value = "${gameState.linesCleared} • ур.${gameState.level}", titleColor = titleColor, subColor = subColor)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "След.",
                                style = MaterialTheme.typography.labelSmall,
                                color = subColor,
                            )
                            NextPiecePreview(
                                type = gameState.nextPieceType,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Игровое поле 10x20
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    val boardModifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(TetrisEngine.BOARD_WIDTH.toFloat() / TetrisEngine.BOARD_HEIGHT.toFloat())

                    GlassCard(
                        modifier = boardModifier,
                        cornerRadius = 18.dp,
                        fillBrush = when {
                            style.isNeon -> Brush.verticalGradient(listOf(Color(0xDD0B1024), Color(0xE6080C1C)))
                            style.isGlass -> Brush.verticalGradient(listOf(Color(0xCF1C1C1C), Color(0xDF101010)))
                            style.isAero -> Brush.verticalGradient(listOf(Color(0xD4073E73), Color(0xE004274E)))
                            style.isTerminal -> Brush.verticalGradient(listOf(Color(0xEA08190A), Color(0xF2040D05)))
                            else -> Brush.verticalGradient(listOf(Color(0xFF10172A), Color(0xFF0B1120)))
                        },
                        borderBrush = when {
                            style.isNeon -> Brush.linearGradient(listOf(Color(0x66A855F7), Color(0x6638BDF8)))
                            style.isGlass -> Brush.linearGradient(listOf(GlassPalette.panelBorderTop, GlassPalette.panelBorderBottom))
                            style.isAero -> AeroPalette.glassBorderBrush()
                            style.isTerminal -> TerminalPalette.activeBorderBrush()
                            else -> Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline))
                        },
                        borderWidth = 1.4.dp,
                        glowColor = when {
                            style.isNeon -> Color(0xFF6366F1)
                            style.isGlass -> Color.White
                            style.isAero -> Color(0xFF38BDF8)
                            style.isTerminal -> TerminalPalette.glowGreen
                            else -> null
                        },
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            TetrisBoardCanvas(
                                state = gameState,
                                onTapRotate = {
                                    gameState = TetrisEngine.rotate(gameState)
                                },
                                onMoveHorizontal = { delta ->
                                    gameState = TetrisEngine.moveHorizontal(gameState, delta)
                                },
                                onSoftDrop = {
                                    gameState = TetrisEngine.softDrop(gameState, bag)
                                },
                                onHardDrop = {
                                    gameState = TetrisEngine.hardDrop(gameState, bag)
                                },
                            )

                            if (gameState.isPaused || gameState.isGameOver) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xB8050816)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    GlassCard(
                                        modifier = Modifier.padding(horizontal = 18.dp),
                                        cornerRadius = 18.dp,
                                        glowColor = when {
                                            style.isNeon -> Color(0xFFA855F7)
                                            style.isGlass -> Color.White
                                            style.isAero -> Color(0xFF38BDF8)
                                            style.isTerminal -> TerminalPalette.glowGreen
                                            else -> null
                                        },
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Text(
                                                text = if (gameState.isGameOver) "Игра окончена" else "Пауза",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (style.isTerminal) TerminalPalette.textPrimary else Color.White,
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Счёт: ${gameState.score} • Рекорд: ${gameState.bestScore}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (style.isTerminal) TerminalPalette.textSecondary else Color(0xFFCBD5E1),
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            GlassCard(
                                                cornerRadius = 12.dp,
                                                fillBrush = when {
                                                    style.isGlass -> Brush.verticalGradient(
                                                        listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
                                                    )
                                                    style.isAero -> AeroPalette.activeFillBrush()
                                                    style.isTerminal -> TerminalPalette.activeFillBrush()
                                                    else -> Brush.verticalGradient(
                                                        listOf(NeonPalette.activeDayStart, NeonPalette.activeDayEnd),
                                                    )
                                                },
                                                borderBrush = if (style.isTerminal) TerminalPalette.activeBorderBrush() else null,
                                                onClick = {
                                                    if (gameState.isGameOver) {
                                                        bag.clear()
                                                        gameState = TetrisEngine.newGame(
                                                            bestScore = gameState.bestScore,
                                                            bag = bag,
                                                        )
                                                    } else {
                                                        gameState = gameState.copy(isPaused = false)
                                                    }
                                                },
                                            ) {
                                                Text(
                                                    text = if (gameState.isGameOver) "Играть снова" else "Продолжить",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (style.isTerminal) TerminalPalette.textPrimary else Color.White,
                                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Панель управления под большими пальцами
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TetrisControlButton(
                        icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Влево",
                        modifier = Modifier.weight(1f),
                        onClick = { gameState = TetrisEngine.moveHorizontal(gameState, -1) },
                    )
                    TetrisControlButton(
                        icon = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Вниз",
                        modifier = Modifier.weight(1f),
                        onClick = { gameState = TetrisEngine.softDrop(gameState, bag) },
                    )
                    TetrisControlButton(
                        icon = Icons.Rounded.KeyboardDoubleArrowDown,
                        contentDescription = "Сбросить вниз",
                        highlighted = true,
                        modifier = Modifier.weight(1f),
                        onClick = { gameState = TetrisEngine.hardDrop(gameState, bag) },
                    )
                    TetrisControlButton(
                        icon = Icons.Rounded.RotateRight,
                        contentDescription = "Повернуть",
                        highlighted = true,
                        modifier = Modifier.weight(1f),
                        onClick = { gameState = TetrisEngine.rotate(gameState) },
                    )
                    TetrisControlButton(
                        icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Вправо",
                        modifier = Modifier.weight(1f),
                        onClick = { gameState = TetrisEngine.moveHorizontal(gameState, 1) },
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Тап по полю — поворот • свайп — движение и сброс",
                    style = MaterialTheme.typography.labelSmall,
                    color = subColor,
                )
            }
        }
    }
}

@Composable
private fun TetrisBoardCanvas(
    state: TetrisGameState,
    onTapRotate: () -> Unit,
    onMoveHorizontal: (Int) -> Unit,
    onSoftDrop: () -> Unit,
    onHardDrop: () -> Unit,
) {
    val stepXThresholdPx = with(LocalDensity.current) { 24.dp.toPx() }
    val stepYThresholdPx = with(LocalDensity.current) { 28.dp.toPx() }
    val hardDropThresholdPx = with(LocalDensity.current) { 82.dp.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTapRotate() })
            }
            .pointerInput(stepXThresholdPx, stepYThresholdPx, hardDropThresholdPx) {
                var accX = 0f
                var accY = 0f
                var totalDownY = 0f
                var droppedThisGesture = false
                detectDragGestures(
                    onDragStart = {
                        accX = 0f
                        accY = 0f
                        totalDownY = 0f
                        droppedThisGesture = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (droppedThisGesture) return@detectDragGestures
                        accX += dragAmount.x
                        accY += dragAmount.y
                        if (dragAmount.y > 0f) {
                            totalDownY += dragAmount.y
                        }

                        while (accX >= stepXThresholdPx) {
                            onMoveHorizontal(1)
                            accX -= stepXThresholdPx
                        }
                        while (accX <= -stepXThresholdPx) {
                            onMoveHorizontal(-1)
                            accX += stepXThresholdPx
                        }

                        if (totalDownY >= hardDropThresholdPx && abs(accX) < stepXThresholdPx) {
                            onHardDrop()
                            droppedThisGesture = true
                        } else if (accY >= stepYThresholdPx) {
                            onSoftDrop()
                            accY -= stepYThresholdPx
                        }
                    },
                )
            },
    ) {
        val cols = TetrisEngine.BOARD_WIDTH
        val rows = TetrisEngine.BOARD_HEIGHT
        val cellW = size.width / cols.toFloat()
        val cellH = size.height / rows.toFloat()

        // Тонкая неоновая сетка
        val gridColor = Color(0x18FFFFFF)
        for (c in 1 until cols) {
            val x = c * cellW
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        }
        for (r in 1 until rows) {
            val y = r * cellH
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }

        // Призрачная тень места приземления фигуры
        if (!state.isGameOver) {
            val ghostR = TetrisEngine.ghostRow(state)
            val ghostPiece = state.activePiece.copy(row = ghostR)
            val ghostColor = Color(state.activePiece.type.colorHex)
            ghostPiece.cells().forEach { (r, c) ->
                if (r in 0 until rows && c in 0 until cols) {
                    val left = c * cellW + 2.5f
                    val top = r * cellH + 2.5f
                    val w = cellW - 5f
                    val h = cellH - 5f
                    drawRoundRect(
                        color = ghostColor.copy(alpha = 0.28f),
                        topLeft = Offset(left, top),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(5f, 5f),
                        style = Stroke(width = 2f),
                    )
                }
            }
        }

        // Закреплённые блоки на поле
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = state.board[r][c] ?: continue
                drawNeonBlock(
                    row = r,
                    col = c,
                    cellW = cellW,
                    cellH = cellH,
                    color = Color(cell.colorHex),
                )
            }
        }

        // Активная падающая фигура
        if (!state.isGameOver) {
            val activeColor = Color(state.activePiece.type.colorHex)
            state.activePiece.cells().forEach { (r, c) ->
                if (r in 0 until rows && c in 0 until cols) {
                    drawNeonBlock(
                        row = r,
                        col = c,
                        cellW = cellW,
                        cellH = cellH,
                        color = activeColor,
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawNeonBlock(
    row: Int,
    col: Int,
    cellW: Float,
    cellH: Float,
    color: Color,
) {
    val pad = 2f
    val left = col * cellW + pad
    val top = row * cellH + pad
    val w = (cellW - pad * 2f).coerceAtLeast(2f)
    val h = (cellH - pad * 2f).coerceAtLeast(2f)
    val radius = CornerRadius(5f, 5f)

    drawRoundRect(
        color = color.copy(alpha = 0.88f),
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = radius,
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.38f),
        topLeft = Offset(left + 1.5f, top + 1.5f),
        size = Size((w - 3f).coerceAtLeast(1f), (h * 0.34f).coerceAtLeast(1f)),
        cornerRadius = CornerRadius(3f, 3f),
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(w, h),
        cornerRadius = radius,
        style = Stroke(width = 1.6f),
    )
}

@Composable
private fun NextPiecePreview(
    type: TetrominoType,
    modifier: Modifier = Modifier,
) {
    val cells = remember(type) { TetrisEngine.cellsFor(type, 0) }
    val minR = cells.minOf { it.first }
    val maxR = cells.maxOf { it.first }
    val minC = cells.minOf { it.second }
    val maxC = cells.maxOf { it.second }
    val spanR = (maxR - minR + 1).coerceAtLeast(1)
    val spanC = (maxC - minC + 1).coerceAtLeast(1)
    val color = Color(type.colorHex)

    Canvas(modifier = modifier) {
        val cellSize = minOf(size.width / 4f, size.height / 3f)
        val totalW = spanC * cellSize
        val totalH = spanR * cellSize
        val offsetX = (size.width - totalW) / 2f
        val offsetY = (size.height - totalH) / 2f
        cells.forEach { (r, c) ->
            val x = offsetX + (c - minC) * cellSize + 1.5f
            val y = offsetY + (r - minR) * cellSize + 1.5f
            val s = (cellSize - 3f).coerceAtLeast(2f)
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(s, s),
                cornerRadius = CornerRadius(3f, 3f),
            )
        }
    }
}

@Composable
private fun TetrisStatItem(
    label: String,
    value: String,
    titleColor: Color,
    subColor: Color,
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = subColor,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = titleColor,
        )
    }
}

@Composable
private fun TetrisIconCircleButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val buttonShape = if (style.isAero || style.isTerminal) RoundedCornerShape(10.dp) else CircleShape
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(buttonShape)
            .background(
                brush = when {
                    style.isNeon -> Brush.verticalGradient(listOf(Color(0xBA242D58), Color(0xBA242D58)))
                    style.isGlass -> Brush.verticalGradient(listOf(GlassPalette.panelFillTop, GlassPalette.panelFillTop))
                    style.isAero -> AeroPalette.panelFillBrush()
                    style.isTerminal -> TerminalPalette.panelFillBrush()
                    else -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
                },
                shape = buttonShape,
            )
            .border(
                width = 1.dp,
                color = when {
                    style.isNeon -> NeonPalette.glassBorderTop
                    style.isGlass -> GlassPalette.panelBorderTop
                    style.isAero -> AeroPalette.borderTop
                    style.isTerminal -> TerminalPalette.borderTop
                    else -> MaterialTheme.colorScheme.outlineVariant
                },
                shape = buttonShape,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = when {
                style.isNeon -> NeonPalette.textPrimary
                style.isGlass -> GlassPalette.textPrimary
                style.isAero -> AeroPalette.textPrimary
                style.isTerminal -> TerminalPalette.iconTint
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun TetrisControlButton(
    icon: ImageVector,
    contentDescription: String,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val style = LocalAppVisualStyle.current
    val fillBrush = when {
        highlighted && style.isNeon -> Brush.verticalGradient(
            listOf(Color(0xCC7E22CE), Color(0xCC4338CA)),
        )
        highlighted && style.isGlass -> Brush.verticalGradient(
            listOf(GlassPalette.activeTabFillTop, GlassPalette.activeTabFillBottom),
        )
        highlighted && style.isAero -> AeroPalette.activeFillBrush()
        highlighted && style.isTerminal -> TerminalPalette.activeFillBrush()
        highlighted -> Brush.verticalGradient(
            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary),
        )
        else -> null
    }

    GlassCard(
        modifier = modifier.height(52.dp),
        cornerRadius = 14.dp,
        fillBrush = fillBrush,
        borderBrush = if (highlighted && style.isTerminal) TerminalPalette.activeBorderBrush() else null,
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = when {
                    style.isTerminal -> TerminalPalette.iconTint
                    highlighted || style.isTranslucent -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.size(26.dp),
            )
        }
    }
}
