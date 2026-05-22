package infrastructure.repositories

import domain.models.*
import domain.ports.GameRepository
import infrastructure.database.DatabaseManager

class GameRepositoryImpl(
    private val db: DatabaseManager
) : GameRepository {

    override fun save(game: Game): Game {
        db.executeUpdate(
            """INSERT INTO games 
               (id, player1_id, player1_name, player2_id, player2_name, secret, status, winner_id) 
               VALUES (?, ?, ?, ?, ?, ?, ?, ?)""",
            listOf<Any>(
                game.id,
                game.player1Id,
                game.player1Name,
                game.player2Id,
                game.player2Name,
                combinationToString(game.secret),
                game.status.name,
                game.winnerId ?: ""
            )
        )

        game.moves.forEach { move ->
            saveMove(game.id, move)
        }
        return game
    }

    override fun findById(id: String): Game? {
        val games = db.executeQuery("SELECT * FROM games WHERE id = ?", listOf<Any>(id))
        if (games.isEmpty()) return null

        val gameData = games.first()
        val movesData = db.executeQuery(
            "SELECT * FROM moves WHERE game_id = ? ORDER BY move_number",
            listOf<Any>(id)
        )
        return mapToGame(gameData, movesData)
    }

    override fun findAll(): List<Game> {
        val allGames = mutableListOf<Game>()
        val gamesData = db.executeQuery("SELECT * FROM games ORDER BY created_at DESC")

        for (gameData in gamesData) {
            val gameId = gameData["id"] as String
            val movesData = db.executeQuery(
                "SELECT * FROM moves WHERE game_id = ? ORDER BY move_number",
                listOf<Any>(gameId)
            )
            allGames.add(mapToGame(gameData, movesData))
        }
        return allGames
    }

    override fun getAllGames(): List<Game> = findAll()

    override fun update(game: Game): Game {
        db.executeUpdate(
            "UPDATE games SET status = ?, winner_id = ? WHERE id = ?",
            listOf<Any>(game.status.name, game.winnerId ?: "", game.id)
        )

        val existingMoves = db.executeQuery(
            "SELECT move_number FROM moves WHERE game_id = ?",
            listOf<Any>(game.id)
        )
        val existingMoveNumbers = existingMoves.map { it["move_number"] as Int }.toSet()

        game.moves.forEach { move ->
            if (move.moveNumber !in existingMoveNumbers) {
                saveMove(game.id, move)
            }
        }
        return game
    }

    override fun delete(id: String): Boolean {
        db.executeUpdate("DELETE FROM games WHERE id = ?", listOf<Any>(id))
        return true
    }

    override fun findByPlayer(playerId: String): List<Game> {
        val playerGames = mutableListOf<Game>()
        val gamesData = db.executeQuery(
            "SELECT * FROM games WHERE player1_id = ? OR player2_id = ? ORDER BY created_at DESC",
            listOf<Any>(playerId, playerId)
        )

        for (gameData in gamesData) {
            val gameId = gameData["id"] as String
            val movesData = db.executeQuery(
                "SELECT * FROM moves WHERE game_id = ? ORDER BY move_number",
                listOf<Any>(gameId)
            )
            playerGames.add(mapToGame(gameData, movesData))
        }
        return playerGames
    }

    private fun saveMove(gameId: String, move: Move) {
        db.executeUpdate(
            """INSERT INTO moves 
               (game_id, move_number, player_id, player_name, guess, black_pins, white_pins) 
               VALUES (?, ?, ?, ?, ?, ?, ?)""",
            listOf<Any>(
                gameId,
                move.moveNumber,
                move.playerId,
                move.playerName,
                combinationToString(move.guess),
                move.feedback.blackPins,
                move.feedback.whitePins
            )
        )
    }

    private fun mapToGame(gameData: Map<String, Any>, movesData: List<Map<String, Any>>): Game {
        val id = gameData["id"]?.toString() ?: ""
        val player1Id = gameData["player1_id"]?.toString() ?: ""
        val player1Name = gameData["player1_name"]?.toString() ?: ""
        val player2Id = gameData["player2_id"]?.toString() ?: ""
        val player2Name = gameData["player2_name"]?.toString() ?: ""
        val secretStr = gameData["secret"]?.toString() ?: ""
        val secret = stringToCombination(secretStr)
        val statusStr = gameData["status"]?.toString() ?: "IN_PROGRESS"
        val status = try {
            GameStatus.valueOf(statusStr)
        } catch (e: Exception) {
            GameStatus.IN_PROGRESS
        }
        val winnerId = gameData["winner_id"]?.toString()

        val moves = movesData.map { mapToMove(it) }

        return Game(
            id = id,
            player1Id = player1Id,
            player1Name = player1Name,
            player2Id = player2Id,
            player2Name = player2Name,
            secret = secret,
            moves = moves,
            status = status,
            winnerId = winnerId?.takeIf { it.isNotEmpty() }
        )
    }

    private fun mapToMove(moveData: Map<String, Any>): Move {
        val moveNumber = (moveData["move_number"] as? Number)?.toInt() ?: 0
        val playerId = moveData["player_id"]?.toString() ?: ""
        val playerName = moveData["player_name"]?.toString() ?: ""
        val guessStr = moveData["guess"]?.toString() ?: ""
        val guess = stringToCombination(guessStr)
        val blackPins = (moveData["black_pins"] as? Number)?.toInt() ?: 0
        val whitePins = (moveData["white_pins"] as? Number)?.toInt() ?: 0
        val timestamp = (moveData["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()

        return Move(
            moveNumber = moveNumber,
            guess = guess,
            feedback = Feedback(blackPins, whitePins),
            playerId = playerId,
            playerName = playerName,
            timestamp = timestamp
        )
    }

    private fun combinationToString(combination: Combination): String {
        return combination.colors.joinToString(",") { it.name }
    }

    private fun stringToCombination(str: String): Combination {
        if (str.isBlank()) return Combination(emptyList())
        val colors = str.split(",").map { Color.valueOf(it.trim()) }
        return Combination(colors)
    }
}