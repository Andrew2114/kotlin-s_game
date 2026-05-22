package domain.models

data class Move(
    val moveNumber: Int,
    val guess: Combination,
    val feedback: Feedback,
    val playerId: String = "",
    val playerName: String = ""
)
