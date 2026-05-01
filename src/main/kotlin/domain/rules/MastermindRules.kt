package domain.rules

import domain.models.Combination
import domain.models.Feedback
import domain.models.Game

interface MastermindRules {
    fun calculateFeedback(secret: Combination, guess: Combination) : Feedback
    fun validateGuess(guess: Combination): Boolean
    fun isGameOver(game: Game): Boolean

    companion object{
        const val MAX_MOVES = 12
        const val CODE_LENGTH = 4
    }
}