# Class Diagram - Mastermind Game
## Homework 1 - Architecture Design

## Class Diagram_1

```mermaid
classDiagram
    direction TB
    
    %% ============ DOMAIN ENTITIES ============
    class Combination {
        <<data class>>
        +List~Color~ colors
        +validate()
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
    
    %% ============ INTERFACE ============
    class MastermindRules {
        <<interface>>
        +calculateFeedback()
        +validateGuess()
        +isGameOver()
        +MAX_MOVES
        +CODE_LENGTH
    }
    
    %% ============ USE CASES ============
    class GameUseCases {
        <<use case>>
        -MastermindRules rules
        -GameRepository repo
        +createGame()
        +makeMove()
        +validateMove()
        +getGameHistory()
    }
    
    %% ============ GUI VIEWS (вместо ViewModel) ============
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
    
    %% ============ СВЯЗИ ============
    Game --> Move
    Game --> Combination
    Move --> Combination
    GameUseCases --> MastermindRules
    GameView --> GameUseCases
    GameView --> Game
    HistoryView --> GameUseCases
    HistoryView --> Game
```

#
#

## Class Diagram_2
```mermaid
classDiagram
    direction TB
    
    %% ============ INTERFACES ============
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
    class StatisticsUseCases {
        <<use case>>
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
    
    %% ============ GUI VIEW (вместо ViewModel) ============
    class StatisticsView {
        <<gui>>
        -StatisticsUseCases useCases
        -String currentPlayerId
        -String currentPlayerName
        +refresh()
        +createPlayerSection()
        +createRankingSection()
    }
    
    %% ============ СВЯЗИ ============
    StatisticsUseCases --> GameRepository
    StatisticsUseCases ..> PlayerStats
    StatisticsView --> StatisticsUseCases
    StatisticsView --> PlayerStats
```
#
#

## Class Diagram_3
```mermaid
classDiagram
    direction TB
    
    %% ============ ИНТЕРФЕЙСЫ ============
    class MastermindRules {
        <<interface>>
        +calculateFeedback()
        +validateGuess()
        +isGameOver()
        +MAX_MOVES
        +CODE_LENGTH
    }
    
    %% ============ ИНФРАСТРУКТУРА (РЕАЛИЗАЦИИ) ============
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
    
    %% ============ СВЯЗИ ============
    MastermindRulesImpl ..|> MastermindRules
    GameRepositoryImpl --> DatabaseManager
```
