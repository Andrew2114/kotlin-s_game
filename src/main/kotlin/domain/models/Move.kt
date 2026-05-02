package domain.models
import java.time.LocalDateTime

data class Move (
    val moveNumber: Int,
    val guess: Combination,
    val feedback: Feedback,
    val timestamp: Long = System.currentTimeMillis()
)