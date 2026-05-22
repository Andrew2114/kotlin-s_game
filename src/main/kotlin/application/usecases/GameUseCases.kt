package application.usecases

import domain.models.*
import domain.ports.GameRepository
import domain.rules.MastermindRules
import java.util.UUID

class GameUseCases(
    private val rules: MastermindRules,
    private val repo: GameRepository
) {
    fun createGame(
        player1Id: String,
        player1Name: String,
        player2Id: String,
        player2Name: String,
        secret: Combination
    ): Game {
        val game = Game(
            id = UUID.randomUUID().toString(),
            player1Id = player1Id,
            player1Name = player1Name,
            player2Id = player2Id,
            player2Name = player2Name,
            secret = secret,
            moves = emptyList(),
            status = GameStatus.IN_PROGRESS,
            winnerId = null
        )
        return repo.save(game)
    }

    fun makeMove(gameId: String, playerId: String, guess: Combination): Move {
        val game = repo.findById(gameId)
            ?: throw IllegalArgumentException("Game with id $gameId not found")

        if (rules.isGameOver(game)) {
            throw IllegalStateException("Game is already over")
        }

        if (!rules.validateGuess(guess)) {
            throw IllegalArgumentException("Invalid guess: ${guess.colors.joinToString { it.name }}")
        }

        val feedback = rules.calculateFeedback(game.secret, guess)
        val moveNumber = game.moves.size + 1

        val playerName = if (playerId == game.player1Id) game.player1Name else game.player2Name

        val move = Move(
            moveNumber = moveNumber,
            guess = guess,
            feedback = feedback,
            playerId = playerId,
            playerName = playerName,
            timestamp = System.currentTimeMillis()
        )

        val newStatus = when {
            feedback.blackPins == MastermindRules.CODE_LENGTH -> GameStatus.WON
            moveNumber >= MastermindRules.MAX_MOVES -> GameStatus.LOST
            else -> GameStatus.IN_PROGRESS
        }

        val winnerId = if (feedback.blackPins == MastermindRules.CODE_LENGTH) playerId else game.winnerId

        val updatedGame = game.copy(
            moves = game.moves + move,
            status = newStatus,
            winnerId = winnerId
        )

        repo.update(updatedGame)
        return move
    }

    fun validateMove(game: Game, guess: Combination): Boolean {
        return rules.validateGuess(guess) && !rules.isGameOver(game)
    }

    fun getGameHistory(playerId: String): List<Game> {
        return repo.findByPlayer(playerId)
    }

    fun getAllGames(): List<Game> {
        return repo.getAllGames()
    }

    fun findById(gameId: String): Game? {
        return repo.findById(gameId)
    }
}