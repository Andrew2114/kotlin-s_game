package domain.models

data class Game(
    val id: String,
    val player1Id: String,
    val player1Name: String,
    val player2Id: String,
    val player2Name: String,
    val secret: Combination,
    val moves: List<Move>,
    val status: GameStatus,
    val winnerId: String? = null
)