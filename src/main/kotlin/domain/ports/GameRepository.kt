package domain.ports
import domain.models.Game

interface GameRepository {
    fun save(game: Game): Game
    fun findById(id: String): Game?
    fun findAll(): List<Game>
    fun update(game: Game): Game
    fun delete(id: String): Boolean
    fun findByPlayer(playerId: String): List<Game>
}