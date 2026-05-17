package infrastructure.repositories

import domain.models.Game
import domain.ports.GameRepository

class InMemoryGameRepository : GameRepository {
    private val games = mutableMapOf<String, Game>()

    override fun save(game: Game): Game {
        games[game.id] = game
        return game
    }

    override fun findById(id: String): Game? = games[id]

    override fun findAll(): List<Game> = games.values.toList()

    override fun getAllGames(): List<Game> = games.values.toList()  // ← добавить

    override fun update(game: Game): Game {
        if (!games.containsKey(game.id)) {
            throw IllegalArgumentException("Game with id ${game.id} not found for update")
        }
        games[game.id] = game
        return game
    }

    override fun delete(id: String): Boolean = games.remove(id) != null

    override fun findByPlayer(playerId: String): List<Game> {
        return games.values.filter { game ->
            game.player1Id == playerId || game.player2Id == playerId
        }
    }
}