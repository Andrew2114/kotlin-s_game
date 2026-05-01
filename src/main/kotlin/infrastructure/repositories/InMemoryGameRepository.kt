package infrastructure.repositories

import domain.models.Game
import domain.ports.GameRepository

class InMemoryGameRepository : GameRepository {
    private val games = mutableListOf<Game>()

    override fun save(game: Game): Game {
        games.add(game)
        return game
    }

    override fun findById(id: String): Game? {
        return games.find { it.id == id }
    }

    override fun findAll(): List<Game> {
        return games.toList()
    }

    override fun update(game: Game): Game {
        val index = games.indexOfFirst { it.id == game.id }
        if (index != -1) {
            games[index] = game
        }
        return game
    }

    override fun delete(id: String): Boolean {
        val removed = games.find { it.id == id }
        return games.remove(removed)
    }

    override fun findByPlayer(playerId: String): List<Game> {
        return games.filter { it.playerId == playerId }
    }
}