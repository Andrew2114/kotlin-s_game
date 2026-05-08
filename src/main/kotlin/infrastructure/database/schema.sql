CREATE TABLE players (
                         id TEXT PRIMARY KEY,
                         name TEXT UNIQUE NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE games (
                       id TEXT PRIMARY KEY,
                       player_id TEXT NOT NULL,
                       player_name TEXT NOT NULL,
                       secret TEXT NOT NULL,
                       status TEXT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (player_id) REFERENCES players(id)
);

CREATE TABLE moves (
                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                       game_id TEXT NOT NULL,
                       move_number INTEGER NOT NULL,
                       guess TEXT NOT NULL,
                       black_pins INTEGER NOT NULL,
                       white_pins INTEGER NOT NULL,
                       timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);