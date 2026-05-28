package infrastructure.database

import java.sql.Connection
import java.sql.DriverManager

class DatabaseManager(private val dbUrl: String = "jdbc:sqlite:mastermind.db") {

    private var connection: Connection? = null

    init {
        connect()
        createTables()
    }

    fun connect() {
        try {
            Class.forName("org.sqlite.JDBC")
            connection = DriverManager.getConnection(dbUrl)
            println("Connected to database: $dbUrl")
        } catch (e: Exception) {
            println("Database connection error: ${e.message}")
        }
    }

    fun executeUpdate(sql: String, params: List<Any> = emptyList()): Int {
        return connection?.prepareStatement(sql).use { stmt ->
            params.forEachIndexed { index, param ->
                stmt?.setObject(index + 1, param)
            }
            stmt?.executeUpdate() ?: 0
        } ?: 0
    }

    fun executeQuery(sql: String, params: List<Any> = emptyList()): List<Map<String, Any>> {
        val result = mutableListOf<Map<String, Any>>()

        connection?.prepareStatement(sql).use { stmt ->
            params.forEachIndexed { index, param ->
                stmt?.setObject(index + 1, param)
            }
            val rs = stmt?.executeQuery()

            while (rs?.next() == true) {
                val row = mutableMapOf<String, Any>()
                val metaData = rs.metaData
                for (i in 1..metaData.columnCount) {
                    val columnName = metaData.getColumnName(i)
                    val value = rs.getObject(i) ?: ""
                    row[columnName] = value
                }
                result.add(row)
            }
            rs?.close()
        }
        return result
    }

    fun close() {
        connection?.close()
    }

    private fun createTables() {
        val createPlayersTable = """
            CREATE TABLE IF NOT EXISTS players (
                id TEXT PRIMARY KEY,
                name TEXT UNIQUE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        val createGamesTable = """
            CREATE TABLE IF NOT EXISTS games (
                id TEXT PRIMARY KEY,
                player1_id TEXT NOT NULL,
                player1_name TEXT NOT NULL,
                player2_id TEXT NOT NULL,
                player2_name TEXT NOT NULL,
                secret TEXT NOT NULL,
                status TEXT NOT NULL,
                winner_id TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        val createMovesTable = """
            CREATE TABLE IF NOT EXISTS moves (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id TEXT NOT NULL,
                move_number INTEGER NOT NULL,
                player_id TEXT NOT NULL,
                player_name TEXT NOT NULL,
                guess TEXT NOT NULL,
                black_pins INTEGER NOT NULL,
                white_pins INTEGER NOT NULL,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
            )
        """.trimIndent()


        executeUpdate(createPlayersTable)
        executeUpdate(createGamesTable)
        executeUpdate(createMovesTable)
        println("Database tables created")
    }
}