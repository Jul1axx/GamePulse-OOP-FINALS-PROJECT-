-- GamePulse MySQL schema
-- Run this in phpMyAdmin or MySQL before launching the Java application.
CREATE DATABASE IF NOT EXISTS gamepulse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE gamepulse;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS games (
    game_id INT AUTO_INCREMENT PRIMARY KEY,
    game_name VARCHAR(120) NOT NULL UNIQUE,
    genre VARCHAR(80) NOT NULL DEFAULT 'Other',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS session_limits (
    limit_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    game_id INT NOT NULL,
    limit_minutes INT NOT NULL DEFAULT 60,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_game_limit UNIQUE (user_id, game_id),
    CONSTRAINT fk_limit_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_limit_game FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    game_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NULL,
    duration_seconds BIGINT NOT NULL DEFAULT 0,
    limit_minutes_at_start INT NOT NULL DEFAULT 60,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sessions_user_start (user_id, start_time),
    INDEX idx_sessions_game (game_id),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_session_game FOREIGN KEY (game_id) REFERENCES games(game_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

INSERT IGNORE INTO games (game_name, genre, is_active) VALUES
('Minecraft', 'Sandbox', TRUE),
('Valorant', 'Tactical Shooter', TRUE),
('GTA V', 'Action / Open World', TRUE),
('League of Legends', 'MOBA', TRUE),
('Counter-Strike 2', 'Tactical Shooter', TRUE),
('Roblox', 'Sandbox', TRUE),
('Dota 2', 'MOBA', TRUE),
('Other', 'Other', TRUE);
