package application.usecases

import application.dtos.PlayerStats
import domain.ports.GameRepository
import domain.models.GameStatus

class StatisticsUseCases(
    private val repo: GameRepository
) {
    fun getWinRate(playerId: String): Double {
        val games = repo.findByPlayer(playerId)
        if (games.isEmpty()) return 0.0

        val wins = games.count {it.status == GameStatus.WON}
        return wins.toDouble() / games.size
    }

    fun getAvgMoves(playerId: String): Double {
        val games = repo.findByPlayer(playerId)
        val wonGames = games.filter {it.status == GameStatus.WON}
        if (wonGames.isEmpty()) return 0.0

        val totalGames = wonGames.sumOf {it.moves.size}
        return totalGames.toDouble() / wonGames.size
    }

    fun getPlayerRanking(): List<PlayerStats> {
        val allGames = repo.findAll()
        val playersMap = allGames.groupBy { it.playerId }

        return playersMap.map {(playerId, games) ->
            val wins = games.count {it.status == GameStatus.WON }
            val gamesPlayed = games.size
            val winRate = if (gamesPlayed > 0) wins.toDouble() / gamesPlayed else 0.0

            val wonGames = games.filter { it.status == GameStatus.WON }
            val avgMoves = if(wonGames.isNotEmpty()) {
                wonGames.sumOf {it.moves.size}.toDouble() / wonGames.size
            } else 0.0

            PlayerStats(
                playerId = playerId,
                playerName = games.first().playerName,
                gamesPlayed = gamesPlayed,
                wins = wins,
                winRate = winRate,
                avgMoves = avgMoves,
                rank = 0
            )
        }.sortedByDescending { it.winRate}
            .mapIndexed { index, stats -> stats.copy(rank = index + 1) }
    }
}


