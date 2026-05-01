import application.usecases.GameUseCases
import application.usecases.StatisticsUseCases
import domain.models.*
import infrastructure.rules.MastermindRulesImpl
import infrastructure.repositories.InMemoryGameRepository
import domain.rules.MastermindRules.Companion.MAX_MOVES
import domain.rules.MastermindRules.Companion.CODE_LENGTH

fun main() {
    val repository = InMemoryGameRepository()
    val rules = MastermindRulesImpl()
    val gameUseCases = GameUseCases(rules, repository)
    val statisticsUseCases = StatisticsUseCases(repository)

    val console = MastermindConsole(gameUseCases, statisticsUseCases)
    console.start()
}

class MastermindConsole(
    private val gameUseCases: GameUseCases,
    private val statisticsUseCases: StatisticsUseCases
) {
    private var currentGame: Game? = null
    private var currentPlayerId: String = "default-player"
    private var currentPlayerName: String = "Player"

    fun start() {
        println("=".repeat(50))
        println("   Добро пожаловать в игру  MASTERMIND")
        println("=".repeat(50))
        println()

        while (true) {
            showMainMenu()
            when (readlnOrNull()?.trim()) {
                "1" -> startNewGame()
                "2" -> makeMove()
                "3" -> showStatistics()
                "4" -> showGameHistory()
                "5" -> {
                    println("Спасибо за игру! До встречи!)")
                    return
                }

                else -> println("Неверный выбор. Попробуйте снова.")
            }
        }
    }

    private fun showMainMenu() {
        println()
        println("-".repeat(50))
        println("Главное Меню")
        println("-".repeat(50))
        println("1. Новая игра")
        println("2. Сделать ход" + if (currentGame == null) " (сначала начните новую игру) " else "")
        println("3. Статистика игрока")
        println("4. История игр")
        println("5. Выход")
        println("-".repeat(50))
        println("Ваш выбор: ")
    }

    private fun startNewGame() {
        println()
        println("Новая игра")
        println("-".repeat(30))

        println("Введите ваше имя: ")
        val name = readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() } ?: "Player"
        currentPlayerName = name
        currentPlayerId = "player_${System.currentTimeMillis()}"

        println("Загадана секретная комбинация из 4 цветов.")
        println("Доступные цвета: ${Color.entries.joinToString { it.name }}")
        println("Комбинация может содержать повторяющиеся цвета.")
        println()
        println("Цель: отгадать комбинацию за $MAX_MOVES ходов")
        println("Черный пин = правильный цвет на правильной позиции")
        println("Белый пин = правильный цвет на неправильной позиции")

        val secret = generateRandomSecret()
        currentGame = gameUseCases.createGame(currentPlayerId, currentPlayerName, secret)

        println()
        println("Игра создана! ID игры: ${currentGame?.id?.take(8)}...")
        println("Попробуйте отгадать комбинацию!")
    }

    private fun makeMove() {
        if (currentGame == null) {
            println()
            println("Ошибка: Сначала начните новую игру (выберите пункт 1)")
            return
        }

        val game = currentGame!!

        if (game.status != GameStatus.IN_PROGRESS) {
            println()
            println("Игра уже закончена. Начните новую игру (пункт 1)")
            currentGame = null
            return
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
            return
        }

        if (!gameUseCases.validateMove(game, guess)) {
            println("Комбинация недействительна. Попробуйте снова.")
            return
        }

        try {
            val move = gameUseCases.makeMove(game.id, guess)
            currentGame = gameUseCases.getGameHistory(currentPlayerId).find { it.id == game.id }
            val updatedGame = currentGame ?: game

            println()
            println("Результат хода: ")
            println("   Черных пинов: ${move.feedback.blackPins}")
            println("   Белых пинов: ${move.feedback.whitePins}")
            println()

            when {
                move.feedback.blackPins == CODE_LENGTH -> {
                    println("Поздравляю! Вы отгадали комбинацию!")
                    println("Количество ходов: ${move.moveNumber}")
                    currentGame = null
                }
                updatedGame.moves.size >= MAX_MOVES -> {
                    println("Игра окончена. Вы использовали все $MAX_MOVES ходов")
                    println("Секретная комбинация: ${updatedGame.secret.colors.joinToString { it.name }}")
                    currentGame = null
                }
                else -> {
                    println("Осталось ходов: ${MAX_MOVES - updatedGame.moves.size}")
                }
            }
        } catch (e: Exception) {
            println("Ошибка: ${e.message}")
        }
    }

    private fun showStatistics() {
        println()
        println("Статистика игрока: $currentPlayerName")
        println("-".repeat(50))

        val ranking = statisticsUseCases.getPlayerRanking()
        val myStats = ranking.find { it.playerId == currentPlayerId }

        if (myStats != null) {
            println("   Игр сыграно: ${myStats.gamesPlayed}")
            println("   Побед: ${myStats.wins}")
            println("   Процент побед: ${String.format("%.1f", myStats.winRate * 100)}%")
            println("   Среднее количество ходов: ${String.format("%.1f", myStats.avgMoves)}")
            println("   Место в рейтинге: ${myStats.rank}")
        } else {
            println("   Нет незавершенных игр")
        }

        println()
        println("Топ-5 игроков:")
        println("-".repeat(30))
        ranking.take(5).forEachIndexed { index, stats ->
            println(
                "${index + 1}. ${stats.playerName} - ${String.format("%.1f", stats.winRate * 100)}%  побед " +
                        "(${stats.wins}/${stats.gamesPlayed})"
            )
        }
    }

    private fun showGameHistory() {
        println()
        println("История игр")
        println("-".repeat(50))

        val games = gameUseCases.getGameHistory(currentPlayerId)

        if (games.isEmpty()) {
            println("   Нет незавершенных игр")
        } else {
            games.forEachIndexed { index, game ->
                val result = when (game.status) {
                    GameStatus.WON -> "Победа!"
                    GameStatus.LOST -> "Поражение!"
                    GameStatus.IN_PROGRESS -> "В процессе!"
                }

                println("${index + 1}. Игра от ${game.id.take(8)}... - $result (${game.moves.size} ходов)")

                if (game.moves.isNotEmpty()) {
                    println("   Секретная комбинация: ${game.secret.colors.joinToString { it.name }}")
                }
            }
        }
    }

    private fun parseGuess(input: String?): Combination? {
        if (input.isNullOrBlank()) return null

        val parts = input.split(",").map { it.trim().uppercase() }
        if (parts.size != CODE_LENGTH) {
            return null
        }

        val colors = parts.mapNotNull { colorName ->
            Color.entries.find { it.name == colorName }
        }

        return if (colors.size == CODE_LENGTH) Combination(colors) else null
    }

    private fun generateRandomSecret() : Combination {
        val colors = List(CODE_LENGTH) {
            Color.entries.random()
        }
        return Combination(colors)
    }

    companion object {
        const val MAX_MOVES = 12
        const val CODE_LENGTH = 4
    }
}