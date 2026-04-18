# Class Diagram - Mastermind Game Administration (Simplified)
## Homework 1 - Architecture Design

## Class Diagram

```mermaid
classDiagram
    %% ============ МОДЕЛИ ДАННЫХ ============
    class Color {
        <<enumeration>>
        RED
        GREEN
        BLUE
        YELLOW
        ORANGE
        PURPLE
        +displayName: String
        +colorCode: String
    }
    
    class Combination {
        +colors: List~Color~
        +validate(): Boolean
        +toString(): String
    }
    
    class Feedback {
        +blackPins: Int
        +whitePins: Int
        +isWinning(): Boolean
    }
    
    class Move {
        +moveNumber: Int
        +guess: Combination
        +feedback: Feedback
        +timestamp: String
    }
    
    class Game {
        +id: String
        +playerId: String
        +playerName: String
        +secretCombination: Combination
        +moves: MutableList~Move~
        +status: String
        +createdAt: String
        +finishedAt: String?
        +addMove(move: Move): Boolean
        +isWon(): Boolean
        +isLost(): Boolean
    }
    
    class Player {
        +id: String
        +name: String
        +registeredAt: String
    }
    
    %% ============ ПРАВИЛА ============
    class MastermindRules {
        +CODE_LENGTH: Int = 4
        +MAX_MOVES: Int = 10
        +calculateFeedback(guess: Combination, secret: Combination): Feedback
        +validateGuess(guess: Combination): Boolean
        -calculateBlackPins(guess: Combination, secret: Combination): Int
        -calculateWhitePins(guess: Combination, secret: Combination): Int
    }
    
    %% ============ СЕРВИСЫ (ВСЯ ЛОГИКА) ============
    class GameService {
        -rules: MastermindRules
        -repository: GameRepository
        +createGame(playerId: String, playerName: String, secret: Combination): Game
        +makeMove(gameId: String, guess: Combination): Move
        +validateMove(gameId: String, guess: Combination): Boolean
        +getGame(gameId: String): Game?
        +getAllGames(): List~Game~
        +getGamesByPlayer(playerId: String): List~Game~
    }
    
    class StatisticsService {
        -repository: GameRepository
        +getWinRate(playerId: String): Double
        +getAverageMovesToWin(playerId: String): Double
        +getTotalGames(playerId: String): Int
        +getWins(playerId: String): Int
        +getLosses(playerId: String): Int
        +getPlayerRanking(): List~PlayerStats~
        +getBestPlayer(): String
    }
    
    class PlayerStats {
        +playerId: String
        +playerName: String
        +gamesPlayed: Int
        +wins: Int
        +winRate: Double
        +avgMoves: Double
    }
    
    %% ============ БАЗА ДАННЫХ ============
    class DatabaseHelper {
        -connection: Connection
        +initDatabase(): Boolean
        +executeUpdate(sql: String, params: List~Any?~): Int
        +executeQuery(sql: String, params: List~Any?~): ResultSet
        +close(): Unit
    }
    
    class GameRepository {
        -db: DatabaseHelper
        +save(game: Game): Boolean
        +findById(id: String): Game?
        +findAll(): List~Game~
        +findByPlayer(playerId: String): List~Game~
        +update(game: Game): Boolean
        +delete(id: String): Boolean
        -mapToGame(rs: ResultSet): Game
        -mapToMove(rs: ResultSet): Move
    }
    
    %% ============ GUI (MVVM упрощенно) ============
    class MainWindow {
        -gameService: GameService
        -statsService: StatisticsService
        +show(): Unit
        +refreshGameList(): Unit
        +refreshStats(): Unit
    }
    
    class GamePanel {
        -currentGame: Game?
        +displayGame(game: Game): Unit
        +onMoveSubmitted(guess: Combination): Unit
        +showFeedback(feedback: Feedback): Unit
    }
    
    class HistoryPanel {
        +displayGames(games: List~Game~): Unit
        +displayStats(stats: PlayerStats): Unit
    }
    
    %% ============ СВЯЗИ ============
    Game *-- Combination : secret
    Game *-- Move : moves
    Move *-- Combination : guess
    Move *-- Feedback : feedback
    
    GameService --> MastermindRules
    GameService --> GameRepository
    StatisticsService --> GameRepository
    
    GameRepository --> DatabaseHelper
    
    MainWindow --> GameService
    MainWindow --> StatisticsService
    MainWindow --> GamePanel
    MainWindow --> HistoryPanel
    
    GamePanel --> GameService
    HistoryPanel --> StatisticsService
