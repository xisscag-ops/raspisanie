package ru.ngieu.raspisanie.domain

enum class TetrominoType(val colorHex: Long) {
    I(0xFF38BDF8), // Неоновый голубой
    O(0xFFFACC15), // Неоновый жёлтый
    T(0xFFA855F7), // Неоновый фиолетовый
    S(0xFF34D399), // Неоновый изумрудный
    Z(0xFFFB7185), // Неоновый розово-красный
    J(0xFF60A5FA), // Неоновый синий
    L(0xFFFB923C), // Неоновый оранжевый
}

data class ActivePiece(
    val type: TetrominoType,
    val rotation: Int,
    val row: Int,
    val col: Int,
) {
    fun cells(): List<Pair<Int, Int>> {
        return TetrisEngine.cellsFor(type, rotation).map { (dr, dc) ->
            (row + dr) to (col + dc)
        }
    }
}

data class TetrisGameState(
    val board: List<List<TetrominoType?>>,
    val activePiece: ActivePiece,
    val nextPieceType: TetrominoType,
    val score: Int = 0,
    val bestScore: Int = 0,
    val linesCleared: Int = 0,
    val level: Int = 1,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
) {
    val dropIntervalMs: Long
        get() = (700L - (level - 1) * 55L).coerceAtLeast(140L)
}

object TetrisEngine {
    const val BOARD_WIDTH = 10
    const val BOARD_HEIGHT = 20

    private val shapes: Map<TetrominoType, List<List<Pair<Int, Int>>>> = mapOf(
        TetrominoType.I to listOf(
            listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3),
            listOf(0 to 2, 1 to 2, 2 to 2, 3 to 2),
            listOf(2 to 0, 2 to 1, 2 to 2, 2 to 3),
            listOf(0 to 1, 1 to 1, 2 to 1, 3 to 1),
        ),
        TetrominoType.O to listOf(
            listOf(0 to 1, 0 to 2, 1 to 1, 1 to 2),
            listOf(0 to 1, 0 to 2, 1 to 1, 1 to 2),
            listOf(0 to 1, 0 to 2, 1 to 1, 1 to 2),
            listOf(0 to 1, 0 to 2, 1 to 1, 1 to 2),
        ),
        TetrominoType.T to listOf(
            listOf(0 to 1, 1 to 0, 1 to 1, 1 to 2),
            listOf(0 to 1, 1 to 1, 1 to 2, 2 to 1),
            listOf(1 to 0, 1 to 1, 1 to 2, 2 to 1),
            listOf(0 to 1, 1 to 0, 1 to 1, 2 to 1),
        ),
        TetrominoType.S to listOf(
            listOf(0 to 1, 0 to 2, 1 to 0, 1 to 1),
            listOf(0 to 1, 1 to 1, 1 to 2, 2 to 2),
            listOf(1 to 1, 1 to 2, 2 to 0, 2 to 1),
            listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1),
        ),
        TetrominoType.Z to listOf(
            listOf(0 to 0, 0 to 1, 1 to 1, 1 to 2),
            listOf(0 to 2, 1 to 1, 1 to 2, 2 to 1),
            listOf(1 to 0, 1 to 1, 2 to 1, 2 to 2),
            listOf(0 to 1, 1 to 0, 1 to 1, 2 to 0),
        ),
        TetrominoType.J to listOf(
            listOf(0 to 0, 1 to 0, 1 to 1, 1 to 2),
            listOf(0 to 1, 0 to 2, 1 to 1, 2 to 1),
            listOf(1 to 0, 1 to 1, 1 to 2, 2 to 2),
            listOf(0 to 1, 1 to 1, 2 to 0, 2 to 1),
        ),
        TetrominoType.L to listOf(
            listOf(0 to 2, 1 to 0, 1 to 1, 1 to 2),
            listOf(0 to 1, 1 to 1, 2 to 1, 2 to 2),
            listOf(1 to 0, 1 to 1, 1 to 2, 2 to 0),
            listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1),
        ),
    )

    fun cellsFor(type: TetrominoType, rotation: Int): List<Pair<Int, Int>> {
        val states = shapes.getValue(type)
        val idx = Math.floorMod(rotation, states.size)
        return states[idx]
    }

    fun emptyBoard(): List<List<TetrominoType?>> {
        return List(BOARD_HEIGHT) { List(BOARD_WIDTH) { null } }
    }

    fun newGame(
        bestScore: Int = 0,
        bag: ArrayDeque<TetrominoType> = ArrayDeque(),
    ): TetrisGameState {
        refillBagIfNeeded(bag)
        val first = bag.removeFirst()
        refillBagIfNeeded(bag)
        val next = bag.removeFirst()
        return TetrisGameState(
            board = emptyBoard(),
            activePiece = spawnPiece(first),
            nextPieceType = next,
            score = 0,
            bestScore = bestScore,
            linesCleared = 0,
            level = 1,
            isPaused = false,
            isGameOver = false,
        )
    }

    fun spawnPiece(type: TetrominoType): ActivePiece {
        return ActivePiece(type = type, rotation = 0, row = 0, col = 3)
    }

    fun isValidPosition(
        board: List<List<TetrominoType?>>,
        piece: ActivePiece,
    ): Boolean {
        return piece.cells().all { (r, c) ->
            r in 0 until BOARD_HEIGHT &&
                c in 0 until BOARD_WIDTH &&
                board[r][c] == null
        }
    }

    fun moveHorizontal(
        state: TetrisGameState,
        deltaCol: Int,
    ): TetrisGameState {
        if (state.isPaused || state.isGameOver) return state
        val moved = state.activePiece.copy(col = state.activePiece.col + deltaCol)
        return if (isValidPosition(state.board, moved)) {
            state.copy(activePiece = moved)
        } else {
            state
        }
    }

    fun rotate(state: TetrisGameState): TetrisGameState {
        if (state.isPaused || state.isGameOver) return state
        if (state.activePiece.type == TetrominoType.O) return state

        val nextRotation = (state.activePiece.rotation + 1) % 4
        val kickOffsets = listOf(0, -1, 1, -2, 2)
        for (dc in kickOffsets) {
            val candidate = state.activePiece.copy(
                rotation = nextRotation,
                col = state.activePiece.col + dc,
            )
            if (isValidPosition(state.board, candidate)) {
                return state.copy(activePiece = candidate)
            }
        }
        return state
    }

    fun ghostRow(state: TetrisGameState): Int {
        var piece = state.activePiece
        while (true) {
            val down = piece.copy(row = piece.row + 1)
            if (isValidPosition(state.board, down)) {
                piece = down
            } else {
                return piece.row
            }
        }
    }

    fun stepDown(
        state: TetrisGameState,
        bag: ArrayDeque<TetrominoType>,
    ): TetrisGameState {
        if (state.isPaused || state.isGameOver) return state
        val moved = state.activePiece.copy(row = state.activePiece.row + 1)
        if (isValidPosition(state.board, moved)) {
            return state.copy(activePiece = moved)
        }
        return lockAndSpawn(state = state, bonusScore = 0, bag = bag)
    }

    fun softDrop(
        state: TetrisGameState,
        bag: ArrayDeque<TetrominoType>,
    ): TetrisGameState {
        if (state.isPaused || state.isGameOver) return state
        val moved = state.activePiece.copy(row = state.activePiece.row + 1)
        return if (isValidPosition(state.board, moved)) {
            val newScore = state.score + 1
            state.copy(
                activePiece = moved,
                score = newScore,
                bestScore = maxOf(state.bestScore, newScore),
            )
        } else {
            lockAndSpawn(state = state, bonusScore = 0, bag = bag)
        }
    }

    fun hardDrop(
        state: TetrisGameState,
        bag: ArrayDeque<TetrominoType>,
    ): TetrisGameState {
        if (state.isPaused || state.isGameOver) return state
        val targetRow = ghostRow(state)
        val distance = (targetRow - state.activePiece.row).coerceAtLeast(0)
        val landed = state.copy(activePiece = state.activePiece.copy(row = targetRow))
        return lockAndSpawn(state = landed, bonusScore = distance * 2, bag = bag)
    }

    private fun lockAndSpawn(
        state: TetrisGameState,
        bonusScore: Int,
        bag: ArrayDeque<TetrominoType>,
    ): TetrisGameState {
        val mutableBoard = state.board.map { it.toMutableList() }.toMutableList()
        for ((r, c) in state.activePiece.cells()) {
            if (r in 0 until BOARD_HEIGHT && c in 0 until BOARD_WIDTH) {
                mutableBoard[r][c] = state.activePiece.type
            }
        }

        val remainingRows = mutableBoard.filter { row -> row.any { it == null } }
        val cleared = BOARD_HEIGHT - remainingRows.size
        val filledBoard = List(cleared) { List<TetrominoType?>(BOARD_WIDTH) { null } } +
            remainingRows.map { it.toList() }

        val linePoints = when (cleared) {
            1 -> 100
            2 -> 300
            3 -> 500
            4 -> 800
            else -> 0
        } * state.level

        val newScore = state.score + bonusScore + linePoints
        val newLines = state.linesCleared + cleared
        val newLevel = (newLines / 10) + 1
        val newBest = maxOf(state.bestScore, newScore)

        val spawned = spawnPiece(state.nextPieceType)
        refillBagIfNeeded(bag)
        val upcoming = bag.removeFirst()

        val canSpawn = isValidPosition(filledBoard, spawned)
        return state.copy(
            board = filledBoard,
            activePiece = spawned,
            nextPieceType = upcoming,
            score = newScore,
            bestScore = newBest,
            linesCleared = newLines,
            level = newLevel,
            isGameOver = !canSpawn,
        )
    }

    private fun refillBagIfNeeded(bag: ArrayDeque<TetrominoType>) {
        if (bag.isEmpty()) {
            TetrominoType.entries.shuffled().forEach { bag.addLast(it) }
        }
    }
}
