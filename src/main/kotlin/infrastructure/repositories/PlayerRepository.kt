package infrastructure.repositories

import infrastructure.database.DatabaseManager

class PlayerRepository(
    private val db: DatabaseManager
) {
    fun save(player: Player): Player {
        db.executeUpdate(
            "INSERT OR REPLACE INTO players (id, name) VALUES (?, ?)",
            listOf(player.id, player.name)
        )
        return player
    }

    fun findById(id: String): Player? {
        val result = db.executeQuery("SELECT * FROM players WHERE id = ?", listOf(id))
        if (result.isEmpty()) return null
        val data = result.first()
        return Player(id = data["id"] as String, name = data["name"] as String)
    }

    fun findByName(name: String): Player? {
        val result = db.executeQuery("SELECT * FROM players WHERE name = ?", listOf(name))
        if (result.isEmpty()) return null
        val data = result.first()
        return Player(id = data["id"] as String, name = data["name"] as String)
    }

    fun findAll(): List<Player> {
        val result = db.executeQuery("SELECT * FROM players ORDER BY name")
        return result.map { data ->
            Player(id = data["id"] as String, name = data["name"] as String)
        }
    }

    fun existsByName(name: String): Boolean {
        return findByName(name) != null
    }
}