package application.usecases

import domain.models.*
import infrastructure.repositories.InMemoryGameRepository
import infrastructure.rules.MastermindRulesImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BasicTest {

    private lateinit var repository: InMemoryGameRepository
    private lateinit var gameUseCases: GameUseCases
    private lateinit var statisticsUseCases: StatisticsUseCases
    private val player1Id = "p1"
    private val player1Name = "Alice"
    private val player2Id = "p2"
    private val player2Name = "Bob"

    @BeforeEach
    fun setUp() {
        repository = InMemoryGameRepository()
        val rules = MastermindRulesImpl()
        gameUseCases = GameUseCases(rules, repository)
        statisticsUseCases = StatisticsUseCases(repository)
    }

    @Test
    fun `createGame should create game with two players`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGameForTwoPlayers(player1Id, player1Name, player2Id, player2Name, secret)

        assertNotNull(game.id)
        assertEquals(player1Id, game.player1Id)
        assertEquals(player1Name, game.player1Name)
        assertEquals(player2Id, game.player2Id)
        assertEquals(player2Name, game.player2Name)
        assertEquals(secret, game.secret)
        assertEquals(GameStatus.IN_PROGRESS, game.status)
    }

    @Test
    fun `makeMove should win the game when guess is correct`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGameForTwoPlayers(player1Id, player1Name, player2Id, player2Name, secret)
        val correctGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        val move = gameUseCases.makeMove(game.id, player2Id, correctGuess)

        assertEquals(4, move.feedback.blackPins)

        val updatedGame = repository.findById(game.id)
        assertEquals(GameStatus.WON, updatedGame?.status)
        assertEquals(player2Id, updatedGame?.winnerId)
    }

    @Test
    fun `makeMove should lose the game after 12 wrong guesses and first player wins`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGameForTwoPlayers(player1Id, player1Name, player2Id, player2Name, secret)
        val wrongGuess = Combination(listOf(Color.PURPLE, Color.PURPLE, Color.PURPLE, Color.PURPLE))

        for (i in 1..12) {
            gameUseCases.makeMove(game.id, player2Id, wrongGuess)
        }

        val updatedGame = repository.findById(game.id)
        assertEquals(GameStatus.LOST, updatedGame?.status)
        assertEquals(player1Id, updatedGame?.winnerId)
    }

    @Test
    fun `getWinRate should return correct win rate for guessing player`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val correctGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        val game1 = gameUseCases.createGameForTwoPlayers(player1Id, player1Name, player2Id, player2Name, secret)
        gameUseCases.makeMove(game1.id, player2Id, correctGuess)

        val game2 = gameUseCases.createGameForTwoPlayers(player1Id, player1Name, "p3", "Charlie", secret)
        gameUseCases.makeMove(game2.id, player2Id, correctGuess)

        val winRate = statisticsUseCases.getWinRate(player2Id)
        assertEquals(1.0, winRate, 0.01)
    }

    @Test
    fun `getPlayerRanking should return ranking with both players when they win`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val correctGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game1 = gameUseCases.createGameForTwoPlayers(player1Id, player1Name, player2Id, player2Name, secret)
        val wrongGuess = Combination(listOf(Color.PURPLE, Color.PURPLE, Color.PURPLE, Color.PURPLE))
        for (i in 1..12) {
            gameUseCases.makeMove(game1.id, player2Id, wrongGuess)
        }

        val game2 = gameUseCases.createGameForTwoPlayers(player1Id, player1Name, "p3", "Charlie", secret)
        gameUseCases.makeMove(game2.id, "p3", correctGuess)

        val ranking = statisticsUseCases.getPlayerRanking()

        assertTrue(ranking.isNotEmpty())
    }
}