package domain.models

data class Game(
    val id: String,
    val playerId: String,
    val playerName: String,
    val secret: Combination,
    val moves: List<Move>,
    val status: GameStatus
)