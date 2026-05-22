package gui

import application.usecases.GameUseCases
import domain.models.Game
import domain.models.GameStatus
import javafx.scene.control.*
import javafx.scene.layout.VBox

class HistoryView(
    private val gameUseCases: GameUseCases,
    private val currentPlayerId: String,
    private val currentPlayerName: String
) {
    val root = VBox(15.0)

    private val gamesList = ListView<Game>()

    init {
        val titleLabel = Label("История Игр - $currentPlayerName")
        titleLabel.style = "-fx-font-weight: bold; -fx-font-size: 16px;"

        loadHistory()

        gamesList.setCellFactory { GameListCell() }
        gamesList.prefHeight = 500.0

        gamesList.setOnMouseClicked {
            val selectedGame = gamesList.selectionModel.selectedItem
            if (selectedGame != null) {
                showGameDetails(selectedGame)
            }
        }

        val refreshButton = Button("Обновить")
        refreshButton.setOnAction { refresh() }

        root.children.addAll(titleLabel, gamesList, refreshButton)
    }

    fun refresh() {
        loadHistory()
    }

    private fun loadHistory() {
        val allGames = gameUseCases.getAllGames()

        val playerGames = allGames.filter { game ->
            game.player1Id == currentPlayerId || game.player2Id == currentPlayerId
        }

        gamesList.items.clear()
        gamesList.items.addAll(playerGames.reversed())
    }

    private fun showGameDetails(game: Game) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Детали игры"
        alert.headerText = "Игра ${game.id.take(8)}"

        val winnerText = when {
            game.winnerId != null -> "Победитель: ${if (game.winnerId == game.player1Id) game.player1Name else game.player2Name}"
            game.status == GameStatus.LOST -> "Победителей нет (исчерпаны ходы)"
            else -> "Игра не завершена"
        }

        val movesText = if (game.moves.isEmpty()) {
            "   Нет ходов"
        } else {
            game.moves.joinToString("\n") { move ->
                "   Ход ${move.moveNumber} (${move.playerName}): ${move.guess.colors.joinToString { it.name }} → " +
                        "Чёрных: ${move.feedback.blackPins}, Белых: ${move.feedback.whitePins}"
            }
        }

        val content = """
            Участники: ${game.player1Name} vs ${game.player2Name}
            $winnerText
            Секретная комбинация: ${game.secret.colors.joinToString { it.name }}
            Всего ходов: ${game.moves.size}
            
            Ходы:
            $movesText
        """.trimIndent()

        alert.contentText = content
        alert.showAndWait()
    }

    inner class GameListCell : ListCell<Game>() {
        override fun updateItem(game: Game?, empty: Boolean) {
            super.updateItem(game, empty)

            if (empty || game == null) {
                text = null
                graphic = null
                return
            }

            val result = when {
                game.winnerId != null -> {
                    val winner = if (game.winnerId == game.player1Id) game.player1Name else game.player2Name
                    "Победа! - $winner"
                }
                game.status == GameStatus.LOST -> "Поражение! (все ходы исчерпаны)"
                else -> "В процессе"
            }

            text = "#${game.id.take(8)} - $result (${game.moves.size} ходов)"
        }
    }
}