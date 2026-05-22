package gui

import application.usecases.GameUseCases
import application.usecases.StatisticsUseCases
import infrastructure.repositories.InMemoryGameRepository
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
        val name1 = askPlayerName("Введите имя первого игрока")
        if (name1.isNullOrBlank()) {
            showError("Имя первого игрока не введено. Приложение закрывается.")
            primaryStage.close()
            return
        }

        val name2 = askPlayerName("Введите имя второго игрока")
        if (name2.isNullOrBlank()) {
            showError("Имя второго игрока не введено. Приложение закрывается.")
            primaryStage.close()
            return
        }

        player1 = Player("player_${System.currentTimeMillis()}_1", name1)
        player2 = Player("player_${System.currentTimeMillis()}_2", name2)

        val repository = InMemoryGameRepository()
        val rules = MastermindRulesImpl()

        gameUseCases = GameUseCases(rules, repository)
        statisticsUseCases = StatisticsUseCases(repository)

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

    private fun showError(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "Ошибка"
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }
}

fun main(): Unit = Application.launch(MainApp::class.java)