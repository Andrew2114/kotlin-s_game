package presentation.console

import application.usecases.GameUseCases
import application.usecases.StatisticsUseCases
import domain.models.Color
import domain.models.Combination
import domain.models.Game
import domain.models.GameStatus
import domain.rules.MastermindRules.Companion.CODE_LENGTH
import domain.rules.MastermindRules.Companion.MAX_MOVES

class MastermindConsole(
    private val gameUseCases: GameUseCases,
    private val statisticsUseCases: StatisticsUseCases
) {
    private var currentGame: Game? = null
    private var currentPlayerId: String = "default-player"
    private var currentPlayerName: String = "Player"
    private val moveHandler = MoveHandler(gameUseCases)

    fun start() {
        println("=".repeat(50))
        println("   Добро пожаловать в игру  MASTERMIND")
        println("=".repeat(50))
        println()

        while (true) {
            showMainMenu()
            when (readlnOrNull()?.trim()) {
                "1" -> startNewGame()
                "2" -> {
                    currentGame = moveHandler.makeMove(currentGame, currentPlayerId)
                }
                "3" -> showStatistics()
                "4" -> showGameHistory()
                "5" -> {
                    println("Спасибо за игру! До встречи!")
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
        print("Ваш выбор: ")
    }

    private fun startNewGame() {
        println()
        println("Новая игра")
        println("-".repeat(30))

        print("Введите ваше имя: ")
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
            println("   Нет завершённых игр")
        }

        println()
        println("Топ-5 игроков:")
        println("-".repeat(30))
        ranking.take(5).forEachIndexed { index, stats ->
            println(
                "${index + 1}. ${stats.playerName} - ${String.format("%.1f", stats.winRate * 100)}% побед " +
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
            println("   Нет завершённых игр")
        } else {
            games.forEachIndexed { index, game ->
                val result = when (game.status) {
                    GameStatus.WON -> "Победа!"
                    GameStatus.LOST -> "Поражение!"
                    GameStatus.IN_PROGRESS -> "В процессе"
                }

                println("${index + 1}. Игра от ${game.id.take(8)}... - $result (${game.moves.size} ходов)")

                if (game.moves.isNotEmpty() && game.status != GameStatus.IN_PROGRESS) {
                    println("   Секретная комбинация: ${game.secret.colors.joinToString { it.name }}")
                }
            }
        }
    }

    private fun generateRandomSecret(): Combination {
        val colors = List(CODE_LENGTH) {
            Color.entries.random()
        }
        return Combination(colors)
    }
}
