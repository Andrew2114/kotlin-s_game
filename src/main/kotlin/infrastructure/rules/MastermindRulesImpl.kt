package infrastructure.rules

import domain.models.*
import domain.rules.MastermindRules

class MastermindRulesImpl : MastermindRules {
    override fun calculateFeedback(secret: Combination, guess: Combination): Feedback {
        var blackCnt = 0
        var whiteCnt = 0

        val secretList = secret.colors.toMutableList()
        val guessList = guess.colors.toMutableList()

        val indicesToRemove = mutableListOf<Int>()
        for (i in secretList.indices) {
            if (secretList[i] == guessList[i]) {
                blackCnt++
                indicesToRemove.add(i)
            }
        }

        indicesToRemove.sortedDescending().forEach { i ->
            secretList.removeAt(i)
            guessList.removeAt(i)
        }

        val secretRemaining = secretList.toMutableList()
        val guessRemaining = guessList.toMutableList()

        for (color in guessRemaining) {
            val index = secretRemaining.indexOf(color)
            if (index != -1) {
                whiteCnt++
                secretRemaining.removeAt(index)
            }
        }

        return Feedback(blackCnt, whiteCnt)
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