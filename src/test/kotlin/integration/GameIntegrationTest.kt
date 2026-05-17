package integration

import application.usecases.GameUseCases
import application.usecases.StatisticsUseCases
import domain.models.*
import infrastructure.repositories.InMemoryGameRepository
import infrastructure.rules.MastermindRulesImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameIntegrationTest {

    private lateinit var repository: InMemoryGameRepository
    private lateinit var gameUseCases: GameUseCases
    private lateinit var statisticsUseCases: StatisticsUseCases
    private val player1Id = "player1"
    private val player1Name = "Alice"
    private val player2Id = "player2"
    private val player2Name = "Bob"

    @BeforeEach
    fun setUp() {
        repository = InMemoryGameRepository()
        val rules = MastermindRulesImpl()
        gameUseCases = GameUseCases(rules, repository)
        statisticsUseCases = StatisticsUseCases(repository)
    }

    @Test
    fun `full game scenario - player1 wins`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame(player1Id, player1Name, player2Id, player2Name, secret)

        val wrongGuess = Combination(listOf(Color.PURPLE, Color.PURPLE, Color.PURPLE, Color.PURPLE))
        val move1 = gameUseCases.makeMove(game.id, player1Id, wrongGuess)

        assertEquals(0, move1.feedback.blackPins)
        assertEquals(0, move1.feedback.whitePins)

        val correctGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val move2 = gameUseCases.makeMove(game.id, player1Id, correctGuess)

        assertEquals(4, move2.feedback.blackPins)

        val updatedGame = repository.findById(game.id)
        assertEquals(GameStatus.WON, updatedGame?.status)
        assertEquals(player1Id, updatedGame?.winnerId)
    }

    @Test
    fun `full game scenario - player2 wins`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame(player1Id, player1Name, player2Id, player2Name, secret)

        val wrongGuess = Combination(listOf(Color.PURPLE, Color.PURPLE, Color.PURPLE, Color.PURPLE))
        gameUseCases.makeMove(game.id, player1Id, wrongGuess)

        val correctGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val move2 = gameUseCases.makeMove(game.id, player2Id, correctGuess)

        assertEquals(4, move2.feedback.blackPins)

        val updatedGame = repository.findById(game.id)
        assertEquals(GameStatus.WON, updatedGame?.status)
        assertEquals(player2Id, updatedGame?.winnerId)
    }

    @Test
    fun `full game scenario - players lose by max moves`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame(player1Id, player1Name, player2Id, player2Name, secret)

        val wrongGuess = Combination(listOf(Color.PURPLE, Color.PURPLE, Color.PURPLE, Color.PURPLE))

        for (i in 1..12) {
            val currentPlayer = if (i % 2 == 1) player1Id else player2Id
            gameUseCases.makeMove(game.id, currentPlayer, wrongGuess)
        }

        val updatedGame = repository.findById(game.id)
        assertEquals(GameStatus.LOST, updatedGame?.status)
        assertNull(updatedGame?.winnerId)
    }

    @Test
    fun `statistics updates correctly after games`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val correctGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        val game1 = gameUseCases.createGame(player1Id, player1Name, player2Id, player2Name, secret)
        val wrongGuess = Combination(listOf(Color.PURPLE, Color.PURPLE, Color.PURPLE, Color.PURPLE))
        gameUseCases.makeMove(game1.id, player1Id, wrongGuess)
        gameUseCases.makeMove(game1.id, player1Id, correctGuess)

        val game2 = gameUseCases.createGame(player1Id, player1Name, "player3", "Charlie", secret)
        gameUseCases.makeMove(game2.id, player1Id, correctGuess)

        val ranking = statisticsUseCases.getPlayerRanking()
        val stats = ranking.find { it.playerId == player1Id }

        assertNotNull(stats)
        assertEquals(2, stats?.gamesPlayed)
        assertEquals(2, stats?.wins)
        assertEquals(1.0, stats!!.winRate, 0.01)
        assertEquals(1.5, stats.avgMoves, 0.01)
    }

    @Test
    fun `getGameHistory should return all games for a player`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        gameUseCases.createGame(player1Id, player1Name, player2Id, player2Name, secret)
        gameUseCases.createGame(player1Id, player1Name, "player3", "Charlie", secret)
        gameUseCases.createGame("player4", "David", player2Id, player2Name, secret)

        val player1Games = gameUseCases.getGameHistory(player1Id)
        val player2Games = gameUseCases.getGameHistory(player2Id)

        assertEquals(2, player1Games.size)
        assertEquals(2, player2Games.size)
    }

    @Test
    fun `cannot make move after game is over`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val correctGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame(player1Id, player1Name, player2Id, player2Name, secret)

        gameUseCases.makeMove(game.id, player1Id, correctGuess)

        val exception = assertThrows(IllegalStateException::class.java) {
            gameUseCases.makeMove(game.id, player2Id, correctGuess)
        }
        assertEquals("Game is already over", exception.message)
    }
}