package infrastructure.rules

import domain.models.Combination
import domain.models.Color
import domain.models.Feedback
import domain.models.Game
import domain.models.GameStatus
import domain.rules.MastermindRules

class MastermindRulesImpl : MastermindRules {

    override fun calculateFeedback(secret: Combination, guess: Combination): Feedback {
        var blackCnt = 0
        var whiteCnt = 0

        val secretList = secret.colors
        val guessList = guess.colors

        val secretRemaining = mutableListOf<Color>()
        val guessRemaining = mutableListOf<Color>()

        for (i in secretList.indices) {
            if (secretList[i] == guessList[i]) {
                blackCnt++
            } else {
                secretRemaining.add(secretList[i])
                guessRemaining.add(guessList[i])
            }
        }

        // ВАЖНО: итерируем по цветам, а не по индексам
        for (color in guessRemaining) {
            if (secretRemaining.contains(color)) {
                whiteCnt++
                secretRemaining.remove(color)  // remove(Object) работает в Kotlin
            }
        }

        return Feedback(blackCnt, whiteCnt)
    }

    override fun validateGuess(guess: Combination): Boolean {
        if (guess.colors.size != MastermindRules.CODE_LENGTH) {
            return false
        }

        val validColors = Color.entries.toSet()
        return guess.colors.all { it in validColors }
    }

    override fun isGameOver(game: Game): Boolean {
        if (game.status != GameStatus.IN_PROGRESS) {
            return true
        }

        if (game.moves.size >= MastermindRules.MAX_MOVES) {
            return true
        }

        return false
    }
}