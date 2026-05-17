package gui

import application.usecases.GameUseCases
import application.usecases.StatisticsUseCases
import infrastructure.database.DatabaseManager
import infrastructure.repositories.GameRepositoryImpl
import infrastructure.repositories.PlayerRepository
import infrastructure.repositories.Player
import infrastructure.rules.MastermindRulesImpl
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.stage.Stage

class MainApp : Application() {

    private lateinit var gameUseCases: GameUseCases
    private lateinit var statisticsUseCases: StatisticsUseCases
    private lateinit var player1: Player
    private lateinit var player2: Player
    private var currentPlayerIndex = 0

    private lateinit var statisticsView: StatisticsView
    private lateinit var gameView: GameView
    private lateinit var historyView: HistoryView
    private lateinit var tabPane: TabPane
    private lateinit var statsTab: Tab
    private lateinit var historyTab: Tab

    override fun start(primaryStage: Stage) {
        val databaseManager = DatabaseManager()
        val repository = GameRepositoryImpl(databaseManager)
        val playerRepository = PlayerRepository(databaseManager)
        val rules = MastermindRulesImpl()

        gameUseCases = GameUseCases(rules, repository)
        statisticsUseCases = StatisticsUseCases(repository)

        val players = selectTwoPlayers(playerRepository)
        if (players == null) {
            primaryStage.close()
            return
        }
        player1 = players.first
        player2 = players.second

        val currentPlayer = player1
        primaryStage.title = "Mastermind Admin - Ходит: ${currentPlayer.name}"

        tabPane = TabPane()

        val gameTab = Tab("Игра")
        gameTab.isClosable = false
        gameView = GameView(
            gameUseCases = gameUseCases,
            player1 = player1,
            player2 = player2,
            onPlayerSwitch = { switchPlayer(primaryStage) }
        )
        gameTab.content = gameView.root

        statsTab = Tab("Статистика")
        statsTab.isClosable = false
        updateStatisticsView()
        statsTab.content = statisticsView.root

        statsTab.setOnSelectionChanged {
            if (statsTab.isSelected) {
                updateStatisticsView()
            }
        }

        historyTab = Tab("История")
        historyTab.isClosable = false
        updateHistoryView()
        historyTab.content = historyView.root

        historyTab.setOnSelectionChanged {
            if (historyTab.isSelected) {
                updateHistoryView()
            }
        }

        tabPane.tabs.addAll(gameTab, statsTab, historyTab)

        val scene = Scene(tabPane, 800.0, 600.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun selectTwoPlayers(playerRepository: PlayerRepository): Pair<Player, Player>? {
        val existingPlayers = playerRepository.findAll()

        fun selectPlayer(title: String): Player? {
            if (existingPlayers.isEmpty()) {
                val name = askPlayerName(title) ?: return null
                val id = "player_${System.currentTimeMillis()}"
                return Player(id, name)
            } else {
                val choiceDialog = ChoiceDialog("Новый игрок", listOf("Новый игрок") + existingPlayers.map { it.name })
                choiceDialog.title = "Mastermind"
                choiceDialog.headerText = title
                choiceDialog.contentText = "Выберите игрока:"
                val result = choiceDialog.showAndWait()
                return when {
                    result.isEmpty -> null
                    result.get() == "Новый игрок" -> {
                        val name = askPlayerName(title) ?: return null
                        val id = "player_${System.currentTimeMillis()}"
                        Player(id, name)
                    }
                    else -> {
                        playerRepository.findByName(result.get())
                    }
                }
            }
        }

        val p1 = selectPlayer("Выберите первого игрока") ?: return null
        val p2 = selectPlayer("Выберите второго игрока") ?: return null

        if (playerRepository.findByName(p1.name) == null) {
            playerRepository.save(p1)
        }
        if (playerRepository.findByName(p2.name) == null && p1.name != p2.name) {
            playerRepository.save(p2)
        }

        return Pair(p1, p2)
    }

    private fun updateStatisticsView() {
        val currentPlayer = if (currentPlayerIndex == 0) player1 else player2
        statisticsView = StatisticsView(
            currentPlayerId = currentPlayer.id,
            currentPlayerName = currentPlayer.name,
            statisticsUseCases = statisticsUseCases
        )
        if (::statsTab.isInitialized) {
            statsTab.content = statisticsView.root
        }
    }

    private fun updateHistoryView() {
        val currentPlayer = if (currentPlayerIndex == 0) player1 else player2
        historyView = HistoryView(
            gameUseCases = gameUseCases,
            currentPlayerId = currentPlayer.id,
            currentPlayerName = currentPlayer.name
        )
        if (::historyTab.isInitialized) {
            historyTab.content = historyView.root
        }
    }

    private fun askPlayerName(title: String): String? {
        val dialog = TextInputDialog()
        dialog.title = "Игрок"
        dialog.headerText = title
        dialog.contentText = "Введите имя игрока:"
        val result = dialog.showAndWait()
        return result.orElse(null)?.takeIf { it.isNotBlank() }
    }

    private fun switchPlayer(primaryStage: Stage) {
        currentPlayerIndex = (currentPlayerIndex + 1) % 2
        val currentPlayer = if (currentPlayerIndex == 0) player1 else player2
        primaryStage.title = "Mastermind Admin - Ходит: ${currentPlayer.name}"
        updateStatisticsView()
        updateHistoryView()
    }
}

fun main() = Application.launch(MainApp::class.java)
