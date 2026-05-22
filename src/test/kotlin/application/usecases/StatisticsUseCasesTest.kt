package application.usecases

import domain.models.*
import infrastructure.repositories.InMemoryGameRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StatisticsUseCasesTest {
    private lateinit var repository: InMemoryGameRepository
    private lateinit var statisticsUseCases: StatisticsUseCases

    @BeforeEach
    fun setUp() {
        repository = InMemoryGameRepository()
        statisticsUseCases = StatisticsUseCases(repository)
    }

    @Test
    fun `getWinRate should return zero when player has no games`() {
        val winRate = statisticsUseCases.getWinRate("non-existent-player")
        assertEquals(0.0, winRate)
    }

    @Test
    fun `getWinRate should return zero when player has only lost games`() {
        val playerId = "player1"
        val secret = Combination(emptyList())

        val game1 = Game("1", playerId, "Alice", secret, emptyList(), GameStatus.LOST)
        val game2 = Game("2", playerId, "Alice", secret, emptyList(), GameStatus.LOST)

        repository.save(game1)
        repository.save(game2)

        val winRate = statisticsUseCases.getWinRate(playerId)
        assertEquals(0.0, winRate)
    }

    @Test
    fun `getWinRate should return one when player has only won games`() {
        val playerId = "player2"
        val secret = Combination(emptyList())

        val game1 = Game("1", playerId, "Bob", secret, emptyList(), GameStatus.WON)
        val game2 = Game("2", playerId, "Bob", secret, emptyList(), GameStatus.WON)

        repository.save(game1)
        repository.save(game2)

        val winRate = statisticsUseCases.getWinRate(playerId)
        assertEquals(1.0, winRate)
    }

    @Test
    fun `getWinRate should calculate correct percentage with mixed results`() {
        val playerId = "player3"
        val secret = Combination(emptyList())

        val game1 = Game("1", playerId, "Charlie", secret, emptyList(), GameStatus.WON)
        val game2 = Game("2", playerId, "Charlie", secret, emptyList(), GameStatus.LOST)
        val game3 = Game("3", playerId, "Charlie", secret, emptyList(), GameStatus.WON)
        val game4 = Game("4", playerId, "Charlie", secret, emptyList(), GameStatus.WON)

        repository.save(game1)
        repository.save(game2)
        repository.save(game3)
        repository.save(game4)

        val winRate = statisticsUseCases.getWinRate(playerId)
        assertEquals(0.75, winRate, 0.001)
    }

    @Test
    fun `getAvgMoves should return zero when player has no won games`() {
        val playerId = "player4"
        val secret = Combination(emptyList())

        val move1 = Move(1, Combination(emptyList()), Feedback(0, 0))
        val move2 = Move(2, Combination(emptyList()), Feedback(0, 0))

        val game = Game("1", playerId, "David", secret, listOf(move1, move2), GameStatus.LOST)
        repository.save(game)

        val avgMoves = statisticsUseCases.getAvgMoves(playerId)
        assertEquals(0.0, avgMoves)
    }

    @Test
    fun `getAvgMoves should calculate average moves correctly for one won game`() {
        val playerId = "player5"
        val secret = Combination(emptyList())

        val move1 = Move(1, Combination(emptyList()), Feedback(0, 0))
        val move2 = Move(2, Combination(emptyList()), Feedback(0, 0))
        val move3 = Move(3, Combination(emptyList()), Feedback(0, 0))

        val game = Game("1", playerId, "Eve", secret, listOf(move1, move2, move3), GameStatus.WON)
        repository.save(game)

        val avgMoves = statisticsUseCases.getAvgMoves(playerId)
        assertEquals(3.0, avgMoves)
    }

    @Test
    fun `getAvgMoves should calculate average moves correctly for multiple won games`() {
        val playerId = "player6"
        val secret = Combination(emptyList())

        val game1Moves = listOf(
            Move(1, Combination(emptyList()), Feedback(0, 0)),
            Move(2, Combination(emptyList()), Feedback(0, 0))
        )
        val game2Moves = listOf(
            Move(1, Combination(emptyList()), Feedback(0, 0)),
            Move(2, Combination(emptyList()), Feedback(0, 0)),
            Move(3, Combination(emptyList()), Feedback(0, 0)),
            Move(4, Combination(emptyList()), Feedback(0, 0))
        )

        val game1 = Game("1", playerId, "Frank", secret, game1Moves, GameStatus.WON)
        val game2 = Game("2", playerId, "Frank", secret, game2Moves, GameStatus.WON)

        repository.save(game1)
        repository.save(game2)

        val avgMoves = statisticsUseCases.getAvgMoves(playerId)
        assertEquals(3.0, avgMoves)
    }

    @Test
    fun `getPlayerRanking should return empty list when no games exist`() {
        val ranking = statisticsUseCases.getPlayerRanking()
        assertTrue(ranking.isEmpty())
    }

    @Test
    fun `getPlayerRanking should return correct ranking sorted by winRate`() {
        val secret = Combination(emptyList())

        val playerA = "playerA"
        repository.save(Game("1", playerA, "Alice", secret, emptyList(), GameStatus.WON))
        repository.save(Game("2", playerA, "Alice", secret, emptyList(), GameStatus.WON))
        repository.save(Game("3", playerA, "Alice", secret, emptyList(), GameStatus.WON))
        repository.save(Game("4", playerA, "Alice", secret, emptyList(), GameStatus.LOST))

        val playerB = "playerB"
        repository.save(Game("5", playerB, "Bob", secret, emptyList(), GameStatus.WON))

        val playerC = "playerC"
        repository.save(Game("6", playerC, "Charlie", secret, emptyList(), GameStatus.LOST))
        repository.save(Game("7", playerC, "Charlie", secret, emptyList(), GameStatus.LOST))

        val ranking = statisticsUseCases.getPlayerRanking()

        assertEquals(3, ranking.size)
        assertEquals("playerB", ranking[0].playerId)
        assertEquals("playerA", ranking[1].playerId)
        assertEquals("playerC", ranking[2].playerId)

        assertEquals(1, ranking[0].rank)
        assertEquals(2, ranking[1].rank)
        assertEquals(3, ranking[2].rank)

        assertEquals(1.0, ranking[0].winRate)
        assertEquals(0.75, ranking[1].winRate, 0.001)
        assertEquals(0.0, ranking[2].winRate)
    }

    @Test
    fun `getPlayerRanking should properly calculate avgMoves`() {
        val secret = Combination(emptyList())
        val playerId = "playerX"

        val movesGame1 = listOf(Move(1, secret, Feedback(0, 0)), Move(2, secret, Feedback(0, 0)))
        val movesGame2 = listOf(Move(1, secret, Feedback(0, 0)))

        repository.save(Game("1", playerId, "Xavier", secret, movesGame1, GameStatus.WON))
        repository.save(Game("2", playerId, "Xavier", secret, movesGame2, GameStatus.WON))

        val ranking = statisticsUseCases.getPlayerRanking()

        assertEquals(1, ranking.size)
        assertEquals(2, ranking[0].gamesPlayed)
        assertEquals(2, ranking[0].wins)
        assertEquals(1.0, ranking[0].winRate)
        assertEquals(1.5, ranking[0].avgMoves)
    }
}