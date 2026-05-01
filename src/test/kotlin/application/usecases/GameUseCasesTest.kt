package application.usecases

import domain.models.*
import domain.rules.MastermindRules
import infrastructure.repositories.InMemoryGameRepository
import infrastructure.rules.MastermindRulesImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameUseCasesTest {

    private lateinit var repository: InMemoryGameRepository
    private lateinit var gameUseCases: GameUseCases

    @BeforeEach
    fun setUp() {
        repository = InMemoryGameRepository()
        val rules = MastermindRulesImpl()
        gameUseCases = GameUseCases(rules, repository)
    }

    @Test
    fun `createGame should create and save a new game`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        val game = gameUseCases.createGame("player1", "Alice", secret)

        assertNotNull(game.id)
        assertEquals("player1", game.playerId)
        assertEquals("Alice", game.playerName)
        assertEquals(secret, game.secret)
        assertEquals(0, game.moves.size)
        assertEquals(GameStatus.IN_PROGRESS, game.status)

        val savedGame = repository.findById(game.id)
        assertNotNull(savedGame)
        assertEquals(game.id, savedGame?.id)
    }

    @Test
    fun `makeMove should create a move and update game correctly for correct guess`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame("player1", "Alice", secret)

        val guess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val move = gameUseCases.makeMove(game.id, guess)

        assertEquals(1, move.moveNumber)
        assertEquals(guess, move.guess)
        assertEquals(4, move.feedback.blackPins)
        assertEquals(0, move.feedback.whitePins)

        val updatedGame = repository.findById(game.id)
        assertEquals(1, updatedGame?.moves?.size)
        assertEquals(GameStatus.WON, updatedGame?.status)
    }

    @Test
    fun `makeMove should create a move and update game correctly for wrong guess`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame("player1", "Alice", secret)

        val guess = Combination(listOf(Color.PURPLE, Color.ORANGE, Color.PURPLE, Color.ORANGE))
        val move = gameUseCases.makeMove(game.id, guess)

        assertEquals(1, move.moveNumber)
        assertEquals(0, move.feedback.blackPins)
        assertEquals(0, move.feedback.whitePins)

        val updatedGame = repository.findById(game.id)
        assertEquals(GameStatus.IN_PROGRESS, updatedGame?.status)
    }

    @Test
    fun `makeMove should throw exception when game is already over`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame("player1", "Alice", secret)

        // Побеждаем в игре
        val winningGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        gameUseCases.makeMove(game.id, winningGuess)

        // Пытаемся сделать ещё ход
        val extraGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        val exception = assertThrows(IllegalStateException::class.java) {
            gameUseCases.makeMove(game.id, extraGuess)
        }
        assertEquals("Game is already over", exception.message)
    }

    @Test
    fun `makeMove should throw exception when game not found`() {
        val guess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            gameUseCases.makeMove("non-existent-id", guess)
        }
        assertTrue(exception.message?.contains("not found") == true)
    }

    @Test
    fun `makeMove should throw exception when guess is invalid`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame("player1", "Alice", secret)

        // Неправильная длина комбинации
        val invalidGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE))

        val exception = assertThrows(IllegalArgumentException::class.java) {
            gameUseCases.makeMove(game.id, invalidGuess)
        }
        assertTrue(exception.message?.contains("Invalid guess") == true)
    }

    @Test
    fun `validateMove should return true for valid move`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame("player1", "Alice", secret)

        val validGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        assertTrue(gameUseCases.validateMove(game, validGuess))
    }

    @Test
    fun `validateMove should return false for invalid guess`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame("player1", "Alice", secret)

        val invalidGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE))

        assertFalse(gameUseCases.validateMove(game, invalidGuess))
    }

    @Test
    fun `validateMove should return false when game is over`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame("player1", "Alice", secret)

        // Завершаем игру
        val winningGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        gameUseCases.makeMove(game.id, winningGuess)

        val updatedGame = repository.findById(game.id)!!
        val anyGuess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        assertFalse(gameUseCases.validateMove(updatedGame, anyGuess))
    }

    @Test
    fun `getGameHistory should return all games for a player`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        gameUseCases.createGame("player1", "Alice", secret)
        gameUseCases.createGame("player1", "Alice", secret)
        gameUseCases.createGame("player2", "Bob", secret)

        val player1Games = gameUseCases.getGameHistory("player1")
        val player2Games = gameUseCases.getGameHistory("player2")
        val player3Games = gameUseCases.getGameHistory("player3")

        assertEquals(2, player1Games.size)
        assertEquals(1, player2Games.size)
        assertEquals(0, player3Games.size)
    }

    @Test
    fun `makeMove should end game with LOST status when max moves reached`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val game = gameUseCases.createGame("player1", "Alice", secret)

        // Делаем 12 неправильных ходов (MAX_MOVES = 12)
        val wrongGuess = Combination(listOf(Color.PURPLE, Color.PURPLE, Color.PURPLE, Color.PURPLE))

        for (i in 1..MastermindRules.MAX_MOVES) {
            gameUseCases.makeMove(game.id, wrongGuess)
        }

        val updatedGame = repository.findById(game.id)!!
        assertEquals(GameStatus.LOST, updatedGame.status)

        // Попытка сделать ещё ход должна выбросить исключение
        val exception = assertThrows(IllegalStateException::class.java) {
            gameUseCases.makeMove(game.id, wrongGuess)
        }
        assertEquals("Game is already over", exception.message)
    }
}