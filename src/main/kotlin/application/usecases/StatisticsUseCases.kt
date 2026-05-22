package application.usecases

import application.dtos.PlayerStats
import domain.models.Game
import domain.models.GameStatus
import domain.ports.GameRepository

class StatisticsUseCases(
    private val repo: GameRepository
) {
    fun getWinRate(playerId: String): Double {
        val games = repo.findByPlayer(playerId)
        if (games.isEmpty()) return 0.0
        val wins = games.count { it.winnerId == playerId }
        return wins.toDouble() / games.size
    }

    fun getAvgMoves(playerId: String): Double {
        val games = repo.findByPlayer(playerId)
        val wonGames = games.filter { it.winnerId == playerId }
        if (wonGames.isEmpty()) return 0.0
        val totalMoves = wonGames.sumOf { it.moves.size }
        return totalMoves.toDouble() / wonGames.size
    }

    fun getPlayerRanking(): List<PlayerStats> {
        val allGames = repo.getAllGames()

        val playersMap = mutableMapOf<String, MutableList<Game>>()

        for (game in allGames) {
            if (!playersMap.containsKey(game.player1Id)) {
                playersMap[game.player1Id] = mutableListOf()
            }
            playersMap[game.player1Id]?.add(game)

            if (!playersMap.containsKey(game.player2Id)) {
                playersMap[game.player2Id] = mutableListOf()
            }
            playersMap[game.player2Id]?.add(game)
        }

        return playersMap.map { (playerId, games) ->
            val firstGame = games.firstOrNull()
            val playerName = when {
                firstGame == null -> "Unknown"
                firstGame.player1Id == playerId -> firstGame.player1Name
                else -> firstGame.player2Name
            }

            val wins = games.count { it.winnerId == playerId }
            val gamesPlayed = games.size
            val winRate = if (gamesPlayed > 0) wins.toDouble() / gamesPlayed else 0.0

            val wonGames = games.filter { it.winnerId == playerId }
            val avgMoves = if (wonGames.isNotEmpty()) {
                wonGames.sumOf { it.moves.size }.toDouble() / wonGames.size
            } else 0.0

            PlayerStats(
                playerId = playerId,
                playerName = playerName,
                gamesPlayed = gamesPlayed,
                wins = wins,
                winRate = winRate,
                avgMoves = avgMoves,
                rank = 0
            )
        }.sortedByDescending { it.winRate }
            .mapIndexed { index, stats -> stats.copy(rank = index + 1) }
    }
}