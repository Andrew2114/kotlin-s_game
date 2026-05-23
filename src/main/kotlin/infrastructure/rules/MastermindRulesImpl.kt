package infrastructure.rules

import domain.models.*
import domain.rules.MastermindRules

class MastermindRulesImpl : MastermindRules {

    override fun calculateFeedback(secret: Combination, guess: Combination): Feedback {
        var blackPins = 0
        var whitePins = 0

        val secretList = secret.colors.toMutableList()
        val guessList = guess.colors.toMutableList()

        val toRemove = mutableListOf<Int>()
        for (i in secretList.indices) {
            if (secretList[i] == guessList[i]) {
                blackPins++
                toRemove.add(i)
            }
        }

        for (i in toRemove.sortedDescending()) {
            secretList.removeAt(i)
            guessList.removeAt(i)
        }

        val secretRemaining = secretList.toMutableList()
        val guessRemaining = guessList.toMutableList()

        for (color in guessRemaining) {
            val index = secretRemaining.indexOf(color)
            if (index != -1) {
                whitePins++
                secretRemaining.removeAt(index)
            }
        }

        return Feedback(blackPins, whitePins)
    }

    override fun validateGuess(guess: Combination): Boolean {
        if (guess.colors.size != MastermindRules.CODE_LENGTH) return false
        val validColors = Color.entries.toSet()
        return guess.colors.all { it in validColors }
    }

    override fun isGameOver(game: Game): Boolean {
        return game.status != GameStatus.IN_PROGRESS || game.moves.size >= MastermindRules.MAX_MOVES
    }
}
