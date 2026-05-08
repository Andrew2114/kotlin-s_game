# Обновленная Архитектура приложения Mastermind

Приложение построено на принципах **Clean Architecture** (Чистая архитектура) с разделением на слои:

1. **Domain** — ядро бизнес-логики
2. **Application** — сценарии использования (Use Cases)
3. **Infrastructure** — реализации интерфейсов (БД, репозитории)
4. **GUI** — графический интерфейс (JavaFX)

## Диаграмма классов

# Class Diagram_1
```mermaid
classDiagram
    direction TB
    
    %% DOMAIN MODELS
    class Game {
        +String id
        +String playerId
        +String playerName
        +Combination secret
        +List~Move~ moves
        +GameStatus status
    }
    
    
    %% DOMAIN INTERFACES
    class MastermindRules {
        <<interface>>
        +calculateFeedback()
        +validateGuess()
        +isGameOver()
    }
    
    class GameRepository {
        <<interface>>
        +save()
        +findById()
        +findAll()
        +update()
        +delete()
        +findByPlayer()
    }
    
    %% APPLICATION USE CASES
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
    
    %% GUI VIEWS
    class GameView {
        <<gui>>
        -GameUseCases useCases
        -Game currentGame
        +makeMove()
        +startNewGame()
        +updateUI()
    }
    
    class HistoryView {
        <<gui>>
        -GameUseCases useCases
        +loadHistory()
        +showGameDetails()
    }
    
    %% СВЯЗИ   
    GameUseCases --> MastermindRules
    GameUseCases --> GameRepository
    GameUseCases ..> Game
    
    StatisticsUseCases --> GameRepository
    StatisticsUseCases ..> Game
    
    GameView --> GameUseCases
    GameView --> Game
    HistoryView --> GameUseCases
    HistoryView --> Game
```

# Class Diagram_2
```mermaid
classDiagram
    direction TB
    
    %% INTERFACES
    class GameRepository {
        <<interface>>
        +save()
        +findById()
        +findAll()
        +findByPlayer()
        +update()
        +delete()
    }
    
    %% USE CASES
    class StatisticsUseCases {
        -GameRepository repo
        +getWinRate()
        +getAvgMoves()
        +getPlayerRanking()
    }
    
    %% DTO
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
    
    %% GUI VIEW
    class StatisticsView {
        <<gui>>
        -StatisticsUseCases useCases
        -String currentPlayerId
        -String currentPlayerName
        +refresh()
        +createPlayerSection()
        +createRankingSection()
    }
    
    %% СВЯЗИ
    StatisticsUseCases --> GameRepository
    StatisticsUseCases ..> PlayerStats
    StatisticsView --> StatisticsUseCases
    StatisticsView --> PlayerStats
```

# Class Diagram_3
```mermaid
classDiagram
    direction TB
    
    %% DOMAIN INTERFACES
    class MastermindRules {
        <<interface>>
        +calculateFeedback()
        +validateGuess()
        +isGameOver()
    }
    
    class GameRepository {
        <<interface>>
        +save()
        +findById()
        +findAll()
        +update()
        +delete()
        +findByPlayer()
    }
    
    %% INFRASTRUCTURE IMPLEMENTATIONS
    class MastermindRulesImpl {
        +calculateFeedback()
        +validateGuess()
        +isGameOver()
        -calculateBlackPins()
        -calculateWhitePins()
    }
    
    class GameRepositoryImpl {
        -DatabaseManager db
        +save()
        +findById()
        +findAll()
        +update()
        +delete()
        +findByPlayer()
        -mapToGame()
        -mapToMove()
        -saveMove()
    }
    
    class PlayerRepository {
        -DatabaseManager db
        +save()
        +findById()
        +findByName()
        +findAll()
        +existsByName()
    }
    
    class DatabaseManager {
        -Connection connection
        +connect()
        +executeUpdate()
        +executeQuery()
        +close()
        -createTables()
    }
    
    %% DATA CLASS
    class Player {
        <<data class>>
        +String id
        +String name
        +Long? createdAt
    }
    
    %% СВЯЗИ
    MastermindRulesImpl ..|> MastermindRules
    GameRepositoryImpl ..|> GameRepository
    
    GameRepositoryImpl --> DatabaseManager
    PlayerRepository --> DatabaseManager
    
    PlayerRepository ..> Player
```