# Class Diagram - Mastermind Game Administration System
## Homework 1 - Architecture Design

## Full Class Diagram

```mermaid
classDiagram
    %% ============ DOMAIN LAYER ============
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
        +fromString(value: String): Color?
    }
    
    class Combination {
        +colors: List~Color~
        +validate(): Boolean
        +toString(): String
        +equals(other: Any?): Boolean
    }
    
    class Feedback {
        +blackPins: Int
        +whitePins: Int
        +isWinning(): Boolean
        +toString(): String
    }
    
    class Move {
        +moveNumber: Int
        +guess: Combination
        +feedback: Feedback
        +timestamp: LocalDateTime
    }
    
    class GameStatus {
        <<enumeration>>
        IN_PROGRESS
        WON
        LOST
    }
    
    class Game {
        +id: String
        +playerId: String
        +secretCombination: Combination
        +moves: MutableList~Move~
        +status: GameStatus
        +createdAt: LocalDateTime
        +finishedAt: LocalDateTime?
        +addMove(move: Move): Boolean
        +isCompleted(): Boolean
        +getCurrentMoveNumber(): Int
    }
    
    class Player {
        +id: String
        +name: String
        +registeredAt: LocalDateTime
    }
    
    %% ============ RULES ============
    class MastermindRules {
        <<interface>>
        +CODE_LENGTH: Int
        +MAX_MOVES: Int
        +calculateFeedback(guess: Combination, secret: Combination): Feedback
        +validateGuess(guess: Combination): Boolean
        +isGameOver(moves: List~Move~): Boolean
    }
    
    class MastermindRulesImpl {
        -calculateBlackPins(guess: Combination, secret: Combination): Int
        -calculateWhitePins(guess: Combination, secret: Combination): Int
    }
    
    %% ============ PORTS ============
    class GameRepository {
        <<interface>>
        +save(game: Game): Game
        +findById(id: String): Game?
        +findAll(): List~Game~
        +findByPlayer(playerId: String): List~Game~
        +delete(id: String): Boolean
        +update(game: Game): Game
    }
    
    class StatisticsCalculator {
        <<interface>>
        +calculateWinRate(playerId: String): Double
        +calculateAverageMovesToWin(playerId: String): Double
        +getTotalGames(playerId: String): Int
        +getWins(playerId: String): Int
        +getLosses(playerId: String): Int
        +getRanking(): List~PlayerStats~
    }
    
    %% ============ APPLICATION LAYER ============
    class CreateGameUseCase {
        +execute(playerId: String, secret: Combination): Game
    }
    
    class MakeMoveUseCase {
        -rules: MastermindRules
        -repository: GameRepository
        +execute(gameId: String, guess: Combination): MoveResult
    }
    
    class ValidateMoveUseCase {
        -rules: MastermindRules
        +execute(guess: Combination, secret: Combination): ValidationResult
    }
    
    class GetGameHistoryUseCase {
        -repository: GameRepository
        +execute(playerId: String?): List~Game~
    }
    
    class GetPlayerStatisticsUseCase {
        -statistics: StatisticsCalculator
        +execute(playerId: String): PlayerStatistics
    }
    
    %% ============ DTOs ============
    class GameDTO {
        +id: String
        +playerName: String
        +movesCount: Int
        +status: String
        +result: String
        +createdAt: LocalDateTime
    }
    
    class PlayerStatistics {
        +playerId: String
        +playerName: String
        +winRate: Double
        +avgMovesToWin: Double
        +totalGames: Int
        +wins: Int
        +losses: Int
        +rank: Int
    }
    
    %% ============ INFRASTRUCTURE ============
    class DatabaseManager {
        -connection: Connection
        +initDatabase(): Boolean
        +executeUpdate(sql: String, params: List~Any?~): Int
        +executeQuery(sql: String, params: List~Any?~): ResultSet
        +close(): Unit
    }
    
    class GameRepositoryImpl {
        -db: DatabaseManager
        +save(game: Game): Game
        +findById(id: String): Game?
        +findAll(): List~Game~
        -mapResultSetToGame(rs: ResultSet): Game
    }
    
    class StatisticsCalculatorImpl {
        -gameRepository: GameRepository
        +calculateWinRate(playerId: String): Double
        +getRanking(): List~PlayerStats~
    }
    
    %% ============ PRESENTATION (MVVM) ============
    class GameViewModel {
        -createGameUseCase: CreateGameUseCase
        -makeMoveUseCase: MakeMoveUseCase
        +currentGame: MutableState~Game?~
        +currentGuess: MutableState~Combination~
        +createNewGame(playerId: String, secret: Combination): Unit
        +makeMove(guess: Combination): Unit
    }
    
    class HistoryViewModel {
        -getHistoryUseCase: GetGameHistoryUseCase
        +games: MutableState~List~GameDTO~~
        +loadHistory(playerId: String?): Unit
    }
    
    class StatisticsViewModel {
        -getStatsUseCase: GetPlayerStatisticsUseCase
        +statistics: MutableState~PlayerStatistics?~
        +loadStatistics(playerId: String): Unit
    }
    
    %% ============ RELATIONSHIPS ============
    Game *-- Combination
    Game *-- Move
    Move *-- Feedback
    Game o-- Player
    
    MastermindRulesImpl ..|> MastermindRules
    GameRepositoryImpl ..|> GameRepository
    StatisticsCalculatorImpl ..|> StatisticsCalculator
    
    CreateGameUseCase --> GameRepository
    MakeMoveUseCase --> MastermindRules
    MakeMoveUseCase --> GameRepository
    GetGameHistoryUseCase --> GameRepository
    GetPlayerStatisticsUseCase --> StatisticsCalculator
    
    DatabaseManager <-- GameRepositoryImpl
    
    GameViewModel --> CreateGameUseCase
    GameViewModel --> MakeMoveUseCase
    HistoryViewModel --> GetGameHistoryUseCase
    StatisticsViewModel --> GetPlayerStatisticsUseCase
