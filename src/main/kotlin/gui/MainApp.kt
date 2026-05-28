package gui

import application.usecases.GameUseCases
import application.usecases.StatisticsUseCases
import domain.models.Combination
import domain.models.Color
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

    private lateinit var gameView: GameView
    private lateinit var statisticsView: StatisticsView
    private lateinit var historyView: HistoryView
    private lateinit var tabPane: TabPane
    private lateinit var statsTab: Tab
    private lateinit var historyTab: Tab

    override fun start(primaryStage: Stage) {
        val name1 = askPlayerName("Введите имя первого игрока (загадывает комбинацию)")
        if (name1.isNullOrBlank()) {
            showError("Имя первого игрока не введено.")
            primaryStage.close()
            return
        }

        val name2 = askPlayerName("Введите имя второго игрока (отгадывает)")
        if (name2.isNullOrBlank()) {
            showError("Имя второго игрока не введено.")
            primaryStage.close()
            return
        }

        val databaseManager = DatabaseManager()
        val repository = GameRepositoryImpl(databaseManager)
        val rules = MastermindRulesImpl()
        val playerRepository = PlayerRepository(databaseManager)

        gameUseCases = GameUseCases(rules, repository)
        statisticsUseCases = StatisticsUseCases(repository)

        player1 = getOrCreatePlayer(name1, playerRepository)
        player2 = getOrCreatePlayer(name2, playerRepository)

        val secret = askSecretCombination("${player1.name}, загадайте секретную комбинацию!")
        if (secret == null) {
            showError("Секретная комбинация не введена.")
            primaryStage.close()
            return
        }

        val currentGame = gameUseCases.createGameForTwoPlayers(
            player1.id, player1.name,
            player2.id, player2.name,
            secret
        )

        primaryStage.title = "Mastermind - Отгадывает: ${player2.name}"

        tabPane = TabPane()

        val gameTab = Tab("Игра")
        gameTab.isClosable = false
        gameView = GameView(
            gameUseCases = gameUseCases,
            player1 = player1,
            player2 = player2,
            initialGame = currentGame
        )
        gameTab.content = gameView.root

        statsTab = Tab("Статистика")
        statsTab.isClosable = false
        updateStatisticsView()
        statsTab.content = statisticsView.root
        statsTab.setOnSelectionChanged { if (statsTab.isSelected) updateStatisticsView() }

        historyTab = Tab("История")
        historyTab.isClosable = false
        updateHistoryView()
        historyTab.content = historyView.root
        historyTab.setOnSelectionChanged { if (historyTab.isSelected) updateHistoryView() }

        tabPane.tabs.addAll(gameTab, statsTab, historyTab)

        val scene = Scene(tabPane, 800.0, 600.0)
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun getOrCreatePlayer(name: String, playerRepository: PlayerRepository): Player {
        val existingPlayer = playerRepository.findByName(name)
        if (existingPlayer != null) {
            println("Игрок $name уже существует, загружаем из БД")
            return existingPlayer
        }
        val id = "player_${System.currentTimeMillis()}"
        val newPlayer = Player(id, name)
        playerRepository.save(newPlayer)
        return newPlayer
    }

    private fun askSecretCombination(title: String): Combination? {
        val dialog = TextInputDialog()
        dialog.title = "Секретная комбинация"
        dialog.headerText = title
        dialog.contentText = "Введите 4 цвета через запятую\nПример: RED,GREEN,BLUE,YELLOW"
        val result = dialog.showAndWait()
        val input = result.orElse(null)?.trim()?.uppercase()
        return parseCombination(input)
    }

    private fun parseCombination(input: String?): Combination? {
        if (input.isNullOrBlank()) return null
        val parts = input.split(",").map { it.trim() }
        if (parts.size != 4) return null
        val colors = parts.mapNotNull { colorName ->
            Color.entries.find { it.name == colorName }
        }
        return if (colors.size == 4) Combination(colors) else null
    }

    private fun updateStatisticsView() {
        val currentPlayer = player2
        statisticsView = StatisticsView(
            currentPlayerId = currentPlayer.id,
            currentPlayerName = currentPlayer.name,
            statisticsUseCases = statisticsUseCases
        )
        if (::statsTab.isInitialized) statsTab.content = statisticsView.root
    }

    private fun updateHistoryView() {
        val currentPlayer = player2
        historyView = HistoryView(
            gameUseCases = gameUseCases,
            currentPlayerId = currentPlayer.id,
            currentPlayerName = currentPlayer.name
        )
        if (::historyTab.isInitialized) historyTab.content = historyView.root
    }

    private fun askPlayerName(title: String): String? {
        val dialog = TextInputDialog()
        dialog.title = "Игрок"
        dialog.headerText = title
        dialog.contentText = "Введите имя игрока:"
        return dialog.showAndWait().orElse(null)?.takeIf { it.isNotBlank() }
    }

    private fun showError(message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            title = "Ошибка"
            headerText = null
            contentText = message
            showAndWait()
        }
    }
}

fun main() = Application.launch(MainApp::class.java)