package presentation.console

import application.usecases.GameUseCases
import domain.models.*
import domain.rules.MastermindRules

class MoveHandler(
    private val gameUseCases: GameUseCases,
    private val currentPlayerId: String,
    private val currentPlayerName: String,
    private val player1Id: String,
    private val player2Id: String,
    private val player1Name: String,
    private val player2Name: String
) {
    fun makeMove(currentGame: Game?, onGameEnd: (Game?) -> Unit, onPlayerSwitch: (String, String) -> Unit): Game? {
        var game = currentGame
        if (game == null) {
            println()
            println("Ошибка: Сначала начните новую игру (выберите пункт 1)")
            return game
        }

        if (game.status != GameStatus.IN_PROGRESS) {
            println()
            println("Игра уже закончена. Начните новую игру (пункт 1)")
            onGameEnd(null)
            return null
        }

        println()
        println("Ход №${game.moves.size + 1} из ${MastermindRules.MAX_MOVES}")
        println("Ходит: $currentPlayerName")
        println("-".repeat(30))
        println("Доступные цвета: ${Color.entries.joinToString { it.name }}")
        println("Пример ввода: RED, GREEN, BLUE, YELLOW")
        print("Введите 4 цвета через запятую: ")

        val input = readlnOrNull()?.trim()
        val guess = parseCombination(input)

        if (guess == null) {
            println("Ошибка: Неверный формат или цвет. Попробуйте снова.")
            return game
        }

        try {
            val move = gameUseCases.makeMove(game.id, currentPlayerId, guess)
            game = gameUseCases.findById(game.id)

            println()
            println("Результат хода: ")
            println("   Черных пинов: ${move.feedback.blackPins}")
            println("   Белых пинов: ${move.feedback.whitePins}")
            println()

            when {
                move.feedback.blackPins == MastermindRules.CODE_LENGTH -> {
                    println("ПОБЕДА! Победил $currentPlayerName!")
                    onGameEnd(null)
                    return null
                }
                (game?.moves?.size ?: 0) >= MastermindRules.MAX_MOVES -> {
                    println("ПОРАЖЕНИЕ! Игроки не отгадали комбинацию")
                    println("Секретная комбинация: ${game?.secret?.colors?.joinToString { it.name }}")
                    onGameEnd(null)
                    return null
                }
                else -> {
                    val newId = if (currentPlayerId == player1Id) player2Id else player1Id
                    val newName = if (currentPlayerName == player1Name) player2Name else player1Name
                    onPlayerSwitch(newId, newName)
                    println("Переход хода к: $newName")
                    return game
                }
            }
        } catch (e: Exception) {
            println("Ошибка: ${e.message}")
            return game
        }
    }

    private fun parseCombination(input: String?): Combination? {
        if (input.isNullOrBlank()) return null

        val parts = input.split(",").map { it.trim().uppercase() }
        if (parts.size != MastermindRules.CODE_LENGTH) return null

        val colors = parts.mapNotNull { colorName ->
            Color.entries.find { it.name == colorName }
        }

        return if (colors.size == MastermindRules.CODE_LENGTH) Combination(colors) else null
    }
}