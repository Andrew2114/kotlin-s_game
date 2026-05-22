# Class Diagram - Mastermind Game Administration (Simplified)
## Homework 1 - Architecture Design

## Class Diagram_1

```mermaid
classDiagram
    direction TB
    
    %% ============ DOMAIN ENTITIES ============
    class Color {
        <<enum>>
        RED
        GREEN
        BLUE
        YELLOW
        ORANGE
        PURPLE
    }
    
    class Combination {
        <<data class>>
        +List~Color~ colors
        +validate()
    }
    
    class Feedback {
        <<data class>>
        +int blackPins
        +int whitePins
        +isWinning()
    }
    
    class Move {
        <<data class>>
        +int moveNumber
        +Combination guess
        +Feedback feedback
        +timestamp
    }
    
    class Game {
        <<entity>>
        +String id
        +String playerId
        +String playerName
        +Combination secret
        +List~Move~ moves
        +GameStatus status
        +addMove()
        +isCompleted()
    }
    
    %% ============ INTERFACES ============
    class MastermindRules {
        <<interface>>
        +calculateFeedback()
        +validateGuess()
        +isGameOver()
        +MAX_MOVES
        +CODE_LENGTH
    }
    
    class GameRepository {
        <<interface>>
        +save()
        +findById()
        +findAll()
        +findByPlayer()
        +update()
        +delete()
    }
    
    %% ============ USE CASES ============
    class GameUseCases {
        -MastermindRules rules
        -GameRepository repo
        +createGame()
        +makeMove()
        +validateMove()
        +getGameHistory()
    }
    
    class StatisticsUseCases {
        -GameRepository repo
        +getWinRate()
        +getAvgMoves()
        +getPlayerRanking()
    }
    
    %% ============ DTO ============
    class PlayerStats {
        <<data class>>
        +String playerId
        +String playerName
        +int gamesPlayed
        +int wins
        +double winRate
        +double avgMoves
        +int rank
    }
    
    %% ============ СВЯЗИ ============
    Combination *-- Color
    Game *-- Move
    Game *-- Combination
    Move *-- Combination
    Move *-- Feedback
    
    GameUseCases --> MastermindRules
    GameUseCases --> GameRepository
    GameUseCases ..> Game
    GameUseCases ..> Move
    
    StatisticsUseCases --> GameRepository
    StatisticsUseCases ..> PlayerStats
```
#
#

## Class Diagram_2
```mermaid
classDiagram
    direction TB
    
    %% ============ ИНТЕРФЕЙСЫ (из ядра) ============
    class GameRepository {
        <<interface>>
        +save()
        +findById()
        +findAll()
        +findByPlayer()
        +update()
        +delete()
    }
    
    class MastermindRules {
        <<interface>>
        +calculateFeedback()
        +validateGuess()
        +isGameOver()
        +MAX_MOVES
        +CODE_LENGTH
    }
    
    %% ============ ИНФРАСТРУКТУРА ============
    class DatabaseManager {
        -Connection conn
        +initDatabase()
        +executeUpdate()
        +executeQuery()
        +close()
    }
    
    class GameRepositoryImpl {
        -DatabaseManager db
        -mapToGame()
        -mapToMove()
        +save()
        +findById()
        +findAll()
    }
    
    class MastermindRulesImpl {
        -calculateBlackPins()
        -calculateWhitePins()
        +calculateFeedback()
        +validateGuess()
        +isGameOver()
    }
    
    %% ============ USE CASES (из ядра) ============
    class GameUseCases {
        -MastermindRules rules
        -GameRepository repo
        +createGame()
        +makeMove()
        +validateMove()
        +getGameHistory()
    }
    
    class StatisticsUseCases {
        -GameRepository repo
        +getWinRate()
        +getAvgMoves()
        +getPlayerRanking()
    }
    
    %% ============ ENTITIES (из ядра) ============
    class Game {
        <<entity>>
        +String id
        +String playerId
        +String playerName
        +Combination secret
        +List~Move~ moves
        +addMove()
    }
    
    class Move {
        <<data class>>
        +int moveNumber
        +Combination guess
        +Feedback feedback
    }
    
    class PlayerStats {
        <<data class>>
        +String playerId
        +String playerName
        +int gamesPlayed
        +int wins
        +double winRate
    }
    
    %% ============ VIEWMODELS ============
    class GameViewModel {
        -GameUseCases useCases
        +currentGame
        +currentGuess
        +createGame()
        +makeMove()
    }
    
    class HistoryViewModel {
        -GameUseCases useCases
        +games
        +loadHistory()
    }
    
    class StatisticsViewModel {
        -StatisticsUseCases useCases
        +statistics
        +ranking
        +loadStatistics()
        +loadRanking()
    }
    
    %% ============ СВЯЗИ ============
    
    %% Реализации интерфейсов
    MastermindRulesImpl ..|> MastermindRules
    GameRepositoryImpl ..|> GameRepository
    
    %% Инфраструктура
    GameRepositoryImpl --> DatabaseManager
    GameRepositoryImpl ..> Game
    GameRepositoryImpl ..> Move
    
    %% Use Cases используют интерфейсы
    GameUseCases --> MastermindRules
    GameUseCases --> GameRepository
    StatisticsUseCases --> GameRepository
    
    %% Use Cases возвращают сущности
    GameUseCases ..> Game
    GameUseCases ..> Move
    StatisticsUseCases ..> PlayerStats
    
    %% ViewModels используют Use Cases
    GameViewModel --> GameUseCases
    HistoryViewModel --> GameUseCases
    StatisticsViewModel --> StatisticsUseCases
    
    %% ViewModel отображает DTO
    StatisticsViewModel ..> PlayerStats
```


