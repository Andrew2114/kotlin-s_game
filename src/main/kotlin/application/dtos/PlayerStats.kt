package application.dtos

data class PlayerStats (
    val playerId: String,
    val playerName: String,
    val gamesPlayed: Int,
    val wins: Int,
    val winRate: Double,
    val avgMoves: Double,
    val rank: Int,
)