package ru.ngieu.raspisanie.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import kotlin.math.abs

enum class SwipeAxisDecision {
    UNDECIDED,
    HORIZONTAL,
    VERTICAL,
}

object SwipeBiasPolicy {
    /**
     * Диапазон углов горизонтального свайпа намеренно шире вертикального:
     * боковой свайп засчитывается при |dx| >= |dy| * 0.65 (~±57° от горизонтали),
     * поэтому даже диагональный свайп пальцем уводит экран вбок, а не скроллит список вверх-вниз.
     */
    const val HORIZONTAL_OVER_VERTICAL_RATIO = 0.65f
    const val VERTICAL_OVER_HORIZONTAL_RATIO = 1.50f
    const val PAGER_TOUCH_SLOP_MULTIPLIER = 0.90f
    const val VERTICAL_TOUCH_SLOP_MULTIPLIER = 1.35f

    fun decide(
        dx: Float,
        dy: Float,
        horizontalSlopPx: Float,
        verticalSlopPx: Float,
    ): SwipeAxisDecision {
        val absDx = abs(dx)
        val absDy = abs(dy)
        return when {
            absDx >= horizontalSlopPx && absDx >= absDy * HORIZONTAL_OVER_VERTICAL_RATIO ->
                SwipeAxisDecision.HORIZONTAL
            absDy >= verticalSlopPx && absDy >= absDx * VERTICAL_OVER_HORIZONTAL_RATIO ->
                SwipeAxisDecision.VERTICAL
            else -> SwipeAxisDecision.UNDECIDED
        }
    }
}

private class ScaledTouchSlopViewConfiguration(
    private val delegate: ViewConfiguration,
    override val touchSlop: Float,
) : ViewConfiguration by delegate

@Stable
class HorizontalSwipeGateState(
    val verticalViewConfig: ViewConfiguration,
) {
    var allowVerticalScroll by mutableStateOf(true)
        internal set

    var allowHorizontalScroll by mutableStateOf(true)
        internal set

    // Синхронный флаг для nestedScroll, чтобы гасить вертикальный скролл и PullToRefresh в тот же кадр
    internal var lockVerticalSync: Boolean = false

    val verticalBlockerNestedScroll: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            return if (lockVerticalSync && available.y != 0f) {
                Offset(0f, available.y)
            } else {
                Offset.Zero
            }
        }
    }
}

@Composable
fun HorizontalBiasPagerContainer(
    canInterruptSettling: () -> Boolean,
    onInterruptSettling: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (
        gateState: HorizontalSwipeGateState,
        allowHorizontalScroll: Boolean,
        allowVerticalScroll: Boolean,
    ) -> Unit,
) {
    val baseViewConfig = LocalViewConfiguration.current
    val horizontalSlopPx = (baseViewConfig.touchSlop * SwipeBiasPolicy.PAGER_TOUCH_SLOP_MULTIPLIER).coerceAtLeast(1f)
    val verticalSlopPx = (baseViewConfig.touchSlop * SwipeBiasPolicy.VERTICAL_TOUCH_SLOP_MULTIPLIER).coerceAtLeast(horizontalSlopPx + 1f)

    val pagerViewConfig = remember(baseViewConfig, horizontalSlopPx) {
        ScaledTouchSlopViewConfiguration(baseViewConfig, horizontalSlopPx)
    }
    val verticalViewConfig = remember(baseViewConfig, verticalSlopPx) {
        ScaledTouchSlopViewConfiguration(baseViewConfig, verticalSlopPx)
    }
    val gateState = remember(verticalViewConfig) {
        HorizontalSwipeGateState(verticalViewConfig)
    }
    val latestCanInterrupt by rememberUpdatedState(canInterruptSettling)
    val latestInterrupt by rememberUpdatedState(onInterruptSettling)

    CompositionLocalProvider(LocalViewConfiguration provides pagerViewConfig) {
        Box(
            modifier = modifier.pointerInput(horizontalSlopPx, verticalSlopPx) {
                awaitEachGesture {
                    gateState.lockVerticalSync = false
                    gateState.allowVerticalScroll = true
                    gateState.allowHorizontalScroll = true
                    try {
                        val down = awaitFirstDown(
                            requireUnconsumed = false,
                            pass = PointerEventPass.Initial,
                        )
                        // Если предыдущий горизонтальный свайп уже почти доехал до страницы,
                        // сразу фиксируем страницу при новом касании, чтобы избежать "вибрации" вбок
                        if (latestCanInterrupt()) {
                            latestInterrupt()
                        }

                        val startPos = down.position
                        var decision = SwipeAxisDecision.UNDECIDED

                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val activeChange = event.changes.firstOrNull { it.id == down.id }
                                ?: event.changes.firstOrNull { it.pressed }
                            if (activeChange == null || !activeChange.pressed) {
                                break
                            }
                            if (decision == SwipeAxisDecision.UNDECIDED) {
                                decision = SwipeBiasPolicy.decide(
                                    dx = activeChange.position.x - startPos.x,
                                    dy = activeChange.position.y - startPos.y,
                                    horizontalSlopPx = horizontalSlopPx,
                                    verticalSlopPx = verticalSlopPx,
                                )
                                when (decision) {
                                    SwipeAxisDecision.HORIZONTAL -> {
                                        gateState.lockVerticalSync = true
                                        gateState.allowVerticalScroll = false
                                        gateState.allowHorizontalScroll = true
                                    }
                                    SwipeAxisDecision.VERTICAL -> {
                                        gateState.lockVerticalSync = false
                                        gateState.allowVerticalScroll = true
                                        gateState.allowHorizontalScroll = false
                                        if (latestCanInterrupt()) {
                                            latestInterrupt()
                                        }
                                    }
                                    SwipeAxisDecision.UNDECIDED -> Unit
                                }
                            }
                        }
                    } finally {
                        gateState.lockVerticalSync = false
                        gateState.allowVerticalScroll = true
                        gateState.allowHorizontalScroll = true
                    }
                }
            },
        ) {
            content(
                gateState,
                gateState.allowHorizontalScroll,
                gateState.allowVerticalScroll,
            )
        }
    }
}

@Composable
fun VerticalListWithHorizontalBias(
    gateState: HorizontalSwipeGateState,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalViewConfiguration provides gateState.verticalViewConfig) {
        Box(modifier = Modifier.nestedScroll(gateState.verticalBlockerNestedScroll)) {
            content()
        }
    }
}
