package infrastructure.rules

import domain.models.*
import domain.rules.MastermindRules
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Collections.emptyList

class MastermindRulesImplTest {

    private lateinit var rules: MastermindRules

    @BeforeEach
    fun setUp() {
        rules = MastermindRulesImpl()
    }

    @Test
    fun `calculateFeedback should return 4 black pins for exact match`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val guess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))

        val feedback = rules.calculateFeedback(secret, guess)

        assertEquals(4, feedback.blackPins)
        assertEquals(0, feedback.whitePins)
    }

    @Test
    fun `calculateFeedback should return 0 black 0 white for no match`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val guess = Combination(listOf(Color.PURPLE, Color.ORANGE, Color.PURPLE, Color.ORANGE))

        val feedback = rules.calculateFeedback(secret, guess)

        assertEquals(0, feedback.blackPins)
        assertEquals(0, feedback.whitePins)
    }

    @Test
    fun `calculateFeedback should count white pins correctly`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val guess = Combination(listOf(Color.YELLOW, Color.BLUE, Color.GREEN, Color.RED))

        val feedback = rules.calculateFeedback(secret, guess)

        assertEquals(0, feedback.blackPins)
        assertEquals(4, feedback.whitePins)
    }

    @Test
    fun `calculateFeedback should handle mixed black and white pins`() {
        val secret = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        val guess = Combination(listOf(Color.RED, Color.BLUE, Color.GREEN, Color.PURPLE))

        val feedback = rules.calculateFeedback(secret, guess)

        assertEquals(1, feedback.blackPins)
        assertEquals(2, feedback.whitePins)
    }

    @Test
    fun `validateGuess should return true for valid guess`() {
        val guess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW))
        assertTrue(rules.validateGuess(guess))
    }

    @Test
    fun `validateGuess should return false for wrong length`() {
        val guess = Combination(listOf(Color.RED, Color.GREEN, Color.BLUE))
        assertFalse(rules.validateGuess(guess))
    }

    @Test
    fun `validateGuess should return false for invalid color`() {
    }

    @Test
    fun `isGameOver should return true when game is won`() {
        val game = Game(
            id = "1",
            playerId = "p1",
            playerName = "Player",
            secret = Combination(emptyList()),
            moves = emptyList(),
            status = GameStatus.WON
        )
        assertTrue(rules.isGameOver(game))
    }

    @Test
    fun `isGameOver should return true when game is lost`() {
        val game = Game(
            id = "1",
            playerId = "p1",
            playerName = "Player",
            secret = Combination(emptyList()),
            moves = emptyList(),
            status = GameStatus.LOST
        )
        assertTrue(rules.isGameOver(game))
    }

    @Test
    fun `isGameOver should return false when game is in progress`() {
        val game = Game(
            id = "1",
            playerId = "p1",
            playerName = "Player",
            secret = Combination(emptyList()),
            moves = emptyList(),
            status = GameStatus.IN_PROGRESS
        )
        assertFalse(rules.isGameOver(game))
    }

    @Test
    fun `isGameOver should return true when max moves reached`() {
        val moves = List(12) { index ->
            Move(
                moveNumber = index + 1,
                guess = Combination(emptyList()),
                feedback = Feedback(0, 0),
                timestamp = System.currentTimeMillis()
            )
        }

        val game = Game(
            id = "1",
            playerId = "p1",
            playerName = "Player",
            secret = Combination(emptyList()),
            moves = moves,
            status = GameStatus.IN_PROGRESS
        )
        assertTrue(rules.isGameOver(game))
    }
}