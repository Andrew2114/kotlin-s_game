package gui

import application.dtos.PlayerStats
import application.usecases.StatisticsUseCases
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.util.Callback

class StatisticsView(
    private val currentPlayerId: String,
    private val currentPlayerName: String,
    private val statisticsUseCases: StatisticsUseCases
) {
    val root = VBox(20.0)

    private lateinit var gamesLabel: Label
    private lateinit var winsLabel: Label
    private lateinit var winRateLabel: Label
    private lateinit var avgMovesLabel: Label
    private lateinit var rankLabel: Label
    private lateinit var tableView: TableView<PlayerStats>

    init {
        val playerSection = createPlayerSection()
        val rankingSection = createRankingSection()
        val refreshButton = Button("Обновить")
        refreshButton.setOnAction { refresh() }

        root.children.addAll(playerSection, rankingSection, refreshButton)
    }

    fun refresh() {
        val ranking = statisticsUseCases.getPlayerRanking()
        val myStats = ranking.find { it.playerId == currentPlayerId }

        gamesLabel.text = "Игр сыграно: ${myStats?.gamesPlayed ?: 0}"
        winsLabel.text = "Побед: ${myStats?.wins ?: 0}"
        winRateLabel.text = "Процент побед: ${String.format("%.1f", (myStats?.winRate ?: 0.0) * 100)}%"
        avgMovesLabel.text = "Среднее число ходов: ${String.format("%.1f", myStats?.avgMoves ?: 0.0)}"
        rankLabel.text = "Место в рейтинге: ${myStats?.rank ?: "—"}"

        val newRanking = statisticsUseCases.getPlayerRanking()
        tableView.items.clear()
        tableView.items.addAll(newRanking.take(5))
    }

    private fun createPlayerSection(): VBox {
        val section = VBox(10.0)
        section.styleClass.add("section")

        val ranking = statisticsUseCases.getPlayerRanking()
        val myStats = ranking.find { it.playerId == currentPlayerId }

        val titleLabel = Label("Статистика Игрока: $currentPlayerName")
        titleLabel.style = "-fx-font-weight: bold; -fx-font-size: 14px;"

        gamesLabel = Label("Игр сыграно: ${myStats?.gamesPlayed ?: 0}")
        winsLabel = Label("Побед: ${myStats?.wins ?: 0}")
        winRateLabel = Label("Процент побед: ${String.format("%.1f", (myStats?.winRate ?: 0.0) * 100)}%")
        avgMovesLabel = Label("Среднее число ходов: ${String.format("%.1f", myStats?.avgMoves ?: 0.0)}")
        rankLabel = Label("Место в рейтинге: ${myStats?.rank ?: "—"}")

        section.children.addAll(
            titleLabel, gamesLabel, winsLabel, winRateLabel, avgMovesLabel, rankLabel
        )
        return section
    }

    private fun createRankingSection(): VBox {
        val section = VBox(10.0)

        val titleLabel = Label("Топ-5 Игроков")
        titleLabel.style = "-fx-font-weight: bold; -fx-font-size: 14px;"

        tableView = TableView<PlayerStats>()
        tableView.prefHeight = 250.0

        val rankColumn = TableColumn<PlayerStats, Int>("Место")
        rankColumn.cellValueFactory = Callback { SimpleObjectProperty(it.value.rank) }
        rankColumn.prefWidth = 60.0

        val nameColumn = TableColumn<PlayerStats, String>("Имя")
        nameColumn.cellValueFactory = Callback { SimpleObjectProperty(it.value.playerName) }
        nameColumn.prefWidth = 150.0

        val winRateColumn = TableColumn<PlayerStats, String>("Побед (%)")
        winRateColumn.cellValueFactory = Callback {
            val stats = it.value
            val percent = String.format("%.0f", stats.winRate * 100)
            val text = "$percent% (${stats.wins}/${stats.gamesPlayed})"
            SimpleObjectProperty(text)
        }
        winRateColumn.prefWidth = 120.0

        val gamesColumn = TableColumn<PlayerStats, Int>("Игр")
        gamesColumn.cellValueFactory = Callback { SimpleObjectProperty(it.value.gamesPlayed) }
        gamesColumn.prefWidth = 60.0

        tableView.columns.addAll(rankColumn, nameColumn, winRateColumn, gamesColumn)

        val ranking = statisticsUseCases.getPlayerRanking()
        tableView.items.addAll(ranking.take(5))

        section.children.addAll(titleLabel, tableView)
        return section
    }
}