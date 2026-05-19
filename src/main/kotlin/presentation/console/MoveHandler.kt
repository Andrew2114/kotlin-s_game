package presentation.console

import application.usecases.GameUseCases
import domain.models.Color
import domain.models.Combination
import domain.models.Game
import domain.models.GameStatus
import domain.rules.MastermindRules.Companion.CODE_LENGTH
import domain.rules.MastermindRules.Companion.MAX_MOVES

class MoveHandler(
    private val gameUseCases: GameUseCases
) {
    fun makeMove(currentGame: Game?, currentPlayerId: String): Game? {
        if (currentGame == null) {
            println()
            println("Ошибка: Сначала начните новую игру (выберите пункт 1)")
            return null
        }

        val game = currentGame

        if (game.status != GameStatus.IN_PROGRESS) {
            println()
            println("Игра уже закончена. Начните новую игру (пункт 1)")
            return null
        }

        println()
        println("Ход №${game.moves.size + 1} из $MAX_MOVES")
        println("-".repeat(30))
        println("Доступные цвета: ${Color.entries.joinToString { it.name }}")
        println("Пример ввода: RED, GREEN, BLUE, YELLOW")
        print("Введите 4 цвета через запятую: ")

        val input = readlnOrNull()?.trim()
        val guess = parseGuess(input)

        if (guess == null) {
            println("Ошибка: Неверный формат или цвет. Попробуйте снова.")
            return currentGame
        }

        if (!gameUseCases.validateMove(game, guess)) {
            println("Комбинация недействительна. Попробуйте снова.")
            return currentGame
        }

        return try {
            val move = gameUseCases.makeMove(game.id, guess)
            val updatedGame = gameUseCases.getGameHistory(currentPlayerId).find { it.id == game.id } ?: game

            println()
            println("Результат хода: ")
            println("   Черных пинов: ${move.feedback.blackPins}")
            println("   Белых пинов: ${move.feedback.whitePins}")
            println()

            when {
                move.feedback.blackPins == CODE_LENGTH -> {
                    println("Поздравляю! Вы отгадали комбинацию!")
                    println("Количество ходов: ${move.moveNumber}")
                    null
                }
                updatedGame.moves.size >= MAX_MOVES -> {
                    println("Игра окончена. Вы использовали все $MAX_MOVES ходов")
                    println("Секретная комбинация: ${updatedGame.secret.colors.joinToString { it.name }}")
                    null
                }
                else -> {
                    println("Осталось ходов: ${MAX_MOVES - updatedGame.moves.size}")
                    updatedGame
                }
            }
        } catch (e: Exception) {
            println("Ошибка: ${e.message}")
            currentGame
        }
    }

    private fun parseGuess(input: String?): Combination? {
        if (input.isNullOrBlank()) return null

        val parts = input.split(",").map { it.trim().uppercase() }
        if (parts.size != CODE_LENGTH) return null

        val colors = parts.mapNotNull { colorName ->
            Color.entries.find { it.name == colorName }
        }

        return if (colors.size == CODE_LENGTH) Combination(colors) else null
    }
}
