package presentation.console

import application.usecases.GameUseCases
import domain.models.*
import domain.rules.MastermindRules.Companion.MAX_MOVES
import domain.rules.MastermindRules.Companion.CODE_LENGTH

class MoveHandler(
    private val gameUseCases: GameUseCases,
    private var currentGame: Game?,
    private var currentPlayerId: String,
    private var currentPlayerName: String,
    private val player1Id: String,
    private val player2Id: String,
    private val player1Name: String,
    private val player2Name: String
) {
    fun handle(): Game? {
        if (currentGame == null) {
            println()
            println("Ошибка: Сначала начните новую игру (выберите пункт 1)")
            return currentGame
        }

        val game = currentGame!!

        if (game.status != GameStatus.IN_PROGRESS) {
            println()
            println("Игра уже закончена. Начните новую игру (пункт 1)")
            return null
        }

        println()
        println("Ход №${game.moves.size + 1} из $MAX_MOVES")
        println("Ходит: $currentPlayerName")
        println("-".repeat(30))
        println("Доступные цвета: ${Color.entries.joinToString { it.name }}")
        println("Пример ввода: RED, GREEN, BLUE, YELLOW")
        print("Введите 4 цвета через запятую: ")

        val input = readlnOrNull()?.trim()
        val guess = parseCombination(input)

        if (guess == null) {
            println("Ошибка: Неверный формат или цвет. Попробуйте снова.")
            return currentGame
        }

        try {
            val move = gameUseCases.makeMove(game.id, currentPlayerId, guess)
            currentGame = gameUseCases.findById(game.id)

            println()
            println("Результат хода: ")
            println("   Черных пинов: ${move.feedback.blackPins}")
            println("   Белых пинов: ${move.feedback.whitePins}")
            println()

            when {
                move.feedback.blackPins == CODE_LENGTH -> {
                    println("ПОБЕДА! Победил $currentPlayerName!")
                    return null
                }
                (currentGame?.moves?.size ?: 0) >= MAX_MOVES -> {
                    println("ПОРАЖЕНИЕ! Игроки не отгадали комбинацию")
                    println("Секретная комбинация: ${game.secret.colors.joinToString { it.name }}")
                    return null
                }
                else -> {
                    currentPlayerId = if (currentPlayerId == player1Id) player2Id else player1Id
                    currentPlayerName = if (currentPlayerName == player1Name) player2Name else player1Name
                    println("Переход хода к: $currentPlayerName")
                    return currentGame
                }
            }
        } catch (e: Exception) {
            println("Ошибка: ${e.message}")
            return currentGame
        }
    }

    private fun parseCombination(input: String?): Combination? {
        if (input.isNullOrBlank()) return null

        val parts = input.split(",").map { it.trim().uppercase() }
        if (parts.size != CODE_LENGTH) return null

        val colors = parts.mapNotNull { colorName ->
            Color.entries.find { it.name == colorName }
        }

        return if (colors.size == CODE_LENGTH) Combination(colors) else null
    }
}