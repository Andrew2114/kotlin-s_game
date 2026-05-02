package application.usecases

import domain.models.*
import domain.ports.GameRepository
import domain.rules.MastermindRules
import java.util.UUID

class GameUseCases(
    private val rules: MastermindRules,
    private val repo: GameRepository
) {
    fun createGame(playerId: String, playerName: String, secret: Combination): Game {
        val game = Game (
            id = UUID.randomUUID().toString(),
            playerId = playerId,
            playerName = playerName,
            secret = secret,
            moves = emptyList(),
            status = GameStatus.IN_PROGRESS
        )

        return repo.save(game)
    }

    fun makeMove(gameId: String, guess: Combination): Move {
        val game = repo.findById(gameId) ?: throw IllegalArgumentException("Game with id $gameId not found")

        if (rules.isGameOver(game) && rules.validateGuess(guess)) {
            throw IllegalStateException("Game is already over")
        }
        if (!rules.validateGuess(guess)) {
            throw IllegalArgumentException("Invalid guess: ${guess.colors}")
        }

        val feedback = rules.calculateFeedback(game.secret, guess)

        val moveNumber = game.moves.size + 1
        val move = Move (
            moveNumber = moveNumber,
            guess = guess,
            feedback = feedback,
            timestamp = System.currentTimeMillis()
        )

        val newStatus = when {
            feedback.blackPins == MastermindRules.CODE_LENGTH -> GameStatus.WON
            moveNumber >= MastermindRules.MAX_MOVES -> GameStatus.LOST
            else -> GameStatus.IN_PROGRESS
        }

        val updateGame = game.copy(
            moves = game.moves + move,
            status = newStatus
        )

        repo.update(updateGame)

        return move
    }

    fun validateMove(game: Game, guess: Combination): Boolean {
        return rules.validateGuess(guess) && !rules.isGameOver(game)
    }

    fun getGameHistory(playerId: String): List<Game> {
        return repo.findByPlayer(playerId)
    }
}

