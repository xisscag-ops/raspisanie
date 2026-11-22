package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.ngieu.raspisanie.ui.components.SwipeAxisDecision
import ru.ngieu.raspisanie.ui.components.SwipeBiasPolicy

class SwipeBiasPolicyTest {

    private val horizontalSlop = 16f
    private val verticalSlop = 32f

    @Test
    fun `favors horizontal swipe on moderate diagonal motion`() {
        // 45 degrees diagonal (dx == dy)
        assertEquals(
            SwipeAxisDecision.HORIZONTAL,
            SwipeBiasPolicy.decide(dx = 22f, dy = 22f, horizontalSlopPx = horizontalSlop, verticalSlopPx = verticalSlop),
        )
        // ~48 degrees from horizontal (dy slightly larger than dx, negative direction)
        assertEquals(
            SwipeAxisDecision.HORIZONTAL,
            SwipeBiasPolicy.decide(dx = -22f, dy = 25f, horizontalSlopPx = horizontalSlop, verticalSlopPx = verticalSlop),
        )
    }

    @Test
    fun `allows comfortable up and down swipes even with slight thumb drift`() {
        assertEquals(
            SwipeAxisDecision.VERTICAL,
            SwipeBiasPolicy.decide(dx = 8f, dy = 60f, horizontalSlopPx = horizontalSlop, verticalSlopPx = verticalSlop),
        )
        assertEquals(
            SwipeAxisDecision.VERTICAL,
            SwipeBiasPolicy.decide(dx = -10f, dy = -70f, horizontalSlopPx = horizontalSlop, verticalSlopPx = verticalSlop),
        )
        // ~35 degrees from vertical (previously hijacked horizontally, now scrolls vertically)
        assertEquals(
            SwipeAxisDecision.VERTICAL,
            SwipeBiasPolicy.decide(dx = 24f, dy = 36f, horizontalSlopPx = horizontalSlop, verticalSlopPx = verticalSlop),
        )
        assertEquals(
            SwipeAxisDecision.VERTICAL,
            SwipeBiasPolicy.decide(dx = -24f, dy = -36f, horizontalSlopPx = horizontalSlop, verticalSlopPx = verticalSlop),
        )
    }

    @Test
    fun `stays undecided for small tap jitter`() {
        assertEquals(
            SwipeAxisDecision.UNDECIDED,
            SwipeBiasPolicy.decide(dx = 6f, dy = 10f, horizontalSlopPx = horizontalSlop, verticalSlopPx = verticalSlop),
        )
    }
}
