# Архитектура приложения Mastermind

## Диаграмма классов

```mermaid
classDiagram
    %% Domain Layer - Models
    class Color {
        <<enumeration>>
        RED
        GREEN
        BLUE
        YELLOW
        ORANGE
        PURPLE
        +getDisplayName()
        +getColorCode()
    }
    
    class Combination {
        +List~Color~ colors
        +validate(): Boolean
        +toString(): String
    }
    
    class Feedback {
        +int blackPins
        +int whitePins
        +isWinning(): Boolean
    }
    
    class Move {
        +int moveNumber
        +Combination guess
        +Feedback feedback
        +LocalDateTime timestamp
    }
    
    class GameStatus {
        <<enumeration>>
        IN_PROGRESS
        WON
        LOST
    }
    
    class Game {
        +String id
        +String playerId
        +Combination secretCombination
        +List~Move~ moves
        +GameStatus status
        +LocalDateTime createdAt
        +LocalDateTime finishedAt
        +isCompleted(): Boolean
    }
    
    class Player {
        +String id
        +String name
        +LocalDateTime registeredAt
    }
    
    %% Domain Layer - Rules
    class MastermindRules {
        <<interface>>
        +validateGuess(guess: Combination, secret: Combination): Boolean
        +calculateFeedback(guess: Combination, secret: Combination): Feedback
        +isGameOver(moves: List~Move~): Boolean
        +MAX_MOVES: int
        +CODE_LENGTH: int
    }
    
    class MastermindRulesImpl {
        -calculateBlackPins()
        -calculateWhitePins()
    }
    
    %% Domain Layer - Ports
    class GameRepository {
        <<interface>>
        +saveGame(game: Game): Game
        +findGameById(id: String): Game?
        +findAllGames(): List~Game~
        +findGamesByPlayer(playerId: String): List~Game~
        +deleteGame(id: String): Boolean
    }
    
    class StatisticsCalculator {
        <<interface>>
        +calculateWinRate(playerId: String): Double
        +calculateAverageMovesToWin(playerId: String): Double
        +getTotalGamesPlayed(playerId: String): Int
        +getPlayerRanking(): List~PlayerStats~
    }
    
    %% Application Layer
    class CreateGameUseCase {
        +execute(playerId: String, secretCombination: Combination): Game
    }
    
    class MakeMoveUseCase {
        -MastermindRules rules
        -GameRepository repository
        +execute(gameId: String, guess: Combination): Move
    }
    
    class ValidateMoveUseCase {
        -MastermindRules rules
        +execute(guess: Combination, secret: Combination): ValidationResult
    }
    
    class GetGameHistoryUseCase {
        -GameRepository repository
        +execute(playerId: String?): List~Game~
    }
    
    class GetPlayerStatisticsUseCase {
        -StatisticsCalculator statsCalculator
        +execute(playerId: String): PlayerStatisticsDTO
    }
    
    %% DTOs
    class GameDTO {
        +String id
        +String playerName
        +List~MoveDTO~ moves
        +String status
        +int movesCount
        +String result
    }
    
    class PlayerStatisticsDTO {
        +double winRate
        +double avgMovesToWin
        +int totalGames
        +int wins
        +int losses
        +int rank
    }
    
    %% Infrastructure Layer
    class DatabaseManager {
        -Connection connection
        +initDatabase()
        +executeQuery()
        +close()
    }
    
    class GameRepositoryImpl {
        -DatabaseManager db
        +saveGame()
        +findGameById()
        +findAllGames()
    }
    
    class StatisticsCalculatorImpl {
        -GameRepository gameRepo
        +calculateWinRate()
        +calculateAverageMovesToWin()
    }
    
    %% Presentation Layer - ViewModels
    class GameViewModel {
        -MakeMoveUseCase makeMoveUseCase
        -CreateGameUseCase createGameUseCase
        +MutableState~Game~ currentGame
        +MutableState~Combination~ currentGuess
        +makeMove()
        +createNewGame()
        +updateGuess(color: Color, position: Int)
    }
    
    class HistoryViewModel {
        -GetGameHistoryUseCase historyUseCase
        +MutableState~List~GameDTO~~ games
        +loadHistory()
    }
    
    class StatisticsViewModel {
        -GetPlayerStatisticsUseCase statsUseCase
        +MutableState~PlayerStatisticsDTO~ stats
        +loadStatistics()
    }
    
    %% Relationships
    Game *-- Combination : secret
    Game *-- Move : moves
    Move *-- Combination : guess
    Move *-- Feedback : feedback
    Game o-- Player : player
    
    MastermindRulesImpl ..|> MastermindRules
    GameRepositoryImpl ..|> GameRepository
    StatisticsCalculatorImpl ..|> StatisticsCalculator
    
    CreateGameUseCase --> GameRepository
    MakeMoveUseCase --> MastermindRules
    MakeMoveUseCase --> GameRepository
    GetGameHistoryUseCase --> GameRepository
    GetPlayerStatisticsUseCase --> StatisticsCalculator
    
    GameViewModel --> MakeMoveUseCase
    GameViewModel --> CreateGameUseCase
    HistoryViewModel --> GetGameHistoryUseCase
    StatisticsViewModel --> GetPlayerStatisticsUseCase
    
    DatabaseManager <-- GameRepositoryImpl
