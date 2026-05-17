CREATE TABLE IF NOT EXISTS players (
                                       id TEXT PRIMARY KEY,
                                       name TEXT UNIQUE NOT NULL,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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
);

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
    );