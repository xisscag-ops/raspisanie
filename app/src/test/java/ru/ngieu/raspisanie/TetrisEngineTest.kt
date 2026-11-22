package ru.ngieu.raspisanie

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.ngieu.raspisanie.domain.ActivePiece
import ru.ngieu.raspisanie.domain.TetrisEngine
import ru.ngieu.raspisanie.domain.TetrisGameState
import ru.ngieu.raspisanie.domain.TetrominoType

class TetrisEngineTest {

    @Test
    fun `respects board walls on horizontal movement and supports wall-kick rotation`() {
        val bag = ArrayDeque(listOf(TetrominoType.I, TetrominoType.T))
        var state = TetrisEngine.newGame(bestScore = 0, bag = bag)
        // Rotate I vertically and push to left wall
        state = TetrisEngine.rotate(state)
        repeat(10) {
            state = TetrisEngine.moveHorizontal(state, -1)
        }
        val minCol = state.activePiece.cells().minOf { it.second }
        assertEquals(0, minCol)

        // Rotating back to horizontal near left wall should succeed via wall kick
        val rotatedBack = TetrisEngine.rotate(state)
        assertTrue(TetrisEngine.isValidPosition(rotatedBack.board, rotatedBack.activePiece))
        assertTrue(rotatedBack.activePiece.cells().all { it.second in 0 until TetrisEngine.BOARD_WIDTH })
    }

    @Test
    fun `clears full line and awards score and updates best score`() {
        val board = TetrisEngine.emptyBoard().map { it.toMutableList() }.toMutableList()
        val bottomRow = TetrisEngine.BOARD_HEIGHT - 1
        // Fill bottom row except columns 4 and 5
        for (c in 0 until TetrisEngine.BOARD_WIDTH) {
            if (c != 4 && c != 5) {
                board[bottomRow][c] = TetrominoType.J
            }
        }
        // Also fill row above except columns 4 and 5 so an O-piece clears 2 lines
        for (c in 0 until TetrisEngine.BOARD_WIDTH) {
            if (c != 4 && c != 5) {
                board[bottomRow - 1][c] = TetrominoType.L
            }
        }

        val state = TetrisGameState(
            board = board.map { it.toList() },
            activePiece = ActivePiece(type = TetrominoType.O, rotation = 0, row = 0, col = 3),
            nextPieceType = TetrominoType.T,
            score = 0,
            bestScore = 50,
            linesCleared = 0,
            level = 1,
        )

        val bag = ArrayDeque(listOf(TetrominoType.S))
        val afterDrop = TetrisEngine.hardDrop(state, bag)

        assertEquals(2, afterDrop.linesCleared)
        assertTrue(afterDrop.score >= 300) // 300 for 2 lines + hard drop bonus
        assertEquals(afterDrop.score, afterDrop.bestScore)
        assertFalse(afterDrop.isGameOver)
        // Bottom row should now be empty after 2-line clear
        assertTrue(afterDrop.board[bottomRow].all { it == null })
    }

    @Test
    fun `detects game over when spawn area is obstructed`() {
        val board = TetrisEngine.emptyBoard().map { it.toMutableList() }.toMutableList()
        // Block spawn area at top
        board[0][4] = TetrominoType.Z
        board[1][4] = TetrominoType.Z

        val state = TetrisGameState(
            board = board.map { it.toList() },
            activePiece = ActivePiece(type = TetrominoType.O, rotation = 0, row = 2, col = 0),
            nextPieceType = TetrominoType.I,
        )
        val bag = ArrayDeque(listOf(TetrominoType.L))
        val afterDrop = TetrisEngine.hardDrop(state, bag)

        assertTrue(afterDrop.isGameOver)
    }
}
