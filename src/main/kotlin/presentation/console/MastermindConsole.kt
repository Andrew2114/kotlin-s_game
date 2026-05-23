package presentation.console

import application.usecases.GameUseCases
import application.usecases.StatisticsUseCases
import domain.models.*
import domain.rules.MastermindRules.Companion.MAX_MOVES
import domain.rules.MastermindRules.Companion.CODE_LENGTH

class MastermindConsole(
    private val gameUseCases: GameUseCases,
    private val statisticsUseCases: StatisticsUseCases
) {
    private var currentGame: Game? = null
    private var currentPlayerId: String = "default-player"
    private var currentPlayerName: String = "Player"
    private var player1Id: String = ""
    private var player2Id: String = ""
    private var player1Name: String = ""
    private var player2Name: String = ""

    fun start() {
        println("=".repeat(50))
        println("   Добро пожаловать в игру MASTERMIND")
        println("=".repeat(50))
        println()

        println("Введите имя первого игрока:")
        player1Name = readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() } ?: "Player1"
        player1Id = "player_${System.currentTimeMillis()}_1"

        println("Введите имя второго игрока:")
        player2Name = readlnOrNull()?.trim()?.takeIf { it.isNotEmpty() } ?: "Player2"
        player2Id = "player_${System.currentTimeMillis()}_2"

        while (true) {
            showMainMenu()
            val input = readlnOrNull()?.trim()

            when (input) {
                "1" -> startNewGame()
                "2" -> makeMove()
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
        println("2. Сделать ход" + if (currentGame == null) " (сначала начните новую игру)" else "")
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

        println("Введите секретную комбинацию из 4 цветов:")
        println("Доступные цвета: ${Color.entries.joinToString { it.name }}")
        println("Пример: RED,GREEN,BLUE,YELLOW")
        print("Введите комбинацию: ")

        val input = readlnOrNull()?.trim()?.uppercase()
        val secret = parseCombination(input)

        if (secret == null) {
            println("Ошибка: неверная комбинация. Игра не создана.")
            return
        }

        currentGame = gameUseCases.createGame(
            player1Id, player1Name,
            player2Id, player2Name,
            secret
        )
        currentPlayerId = player1Id
        currentPlayerName = player1Name

        println()
        println("Игра создана! Ходит: $currentPlayerName")
        println("Попробуйте отгадать комбинацию!")
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

    private fun makeMove() {
        val handler = MoveHandler(
            gameUseCases,
            currentGame,
            currentPlayerId,
            currentPlayerName,
            player1Id,
            player2Id,
            player1Name,
            player2Name
        )
        currentGame = handler.handle()
        if (currentGame == null) {
            // Игра завершена
            return
        }
        // Обновляем текущего игрока после хода (если нужно)
        // currentPlayerId и currentPlayerName должны обновляться внутри MoveHandler
        // Этот код требует доработки, так как MoveHandler не возвращает обновлённые playerId/playerName
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
            println("   Среднее число ходов: ${String.format("%.1f", myStats.avgMoves)}")
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
                val winner = game.winnerId
                val result = when {
                    winner == currentPlayerId -> "Победа!"
                    game.status == GameStatus.LOST -> "Поражение!"
                    else -> "В процессе"
                }
                println("${index + 1}. Игра ${game.id.take(8)} - $result (${game.moves.size} ходов)")
            }
        }
    }
}