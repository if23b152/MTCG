-- Datenbankschema des Monster Trading Cards Game
-- Benutzer werden über ihren Benutzernamen referenziert (user_id = users.username),
-- so wie es die Repositories im Code erwarten.

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    coins    INTEGER DEFAULT 20,
    name     VARCHAR(255),
    bio      TEXT,
    image    VARCHAR(255),
    elo      INTEGER DEFAULT 100,
    wins     INTEGER DEFAULT 0,
    losses   INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS packages (
    id         SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acquired   BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS cards (
    id     UUID PRIMARY KEY,
    name   VARCHAR(255) NOT NULL,
    damage DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS user_cards (
    user_id VARCHAR(255) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
    card_id UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, card_id)
);

CREATE TABLE IF NOT EXISTS decks (
    user_id VARCHAR(255) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
    card_id UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, card_id)
);

CREATE TABLE IF NOT EXISTS trading_deals (
    id             UUID PRIMARY KEY,
    card_to_trade  UUID NOT NULL REFERENCES cards(id) ON DELETE CASCADE,
    user_id        VARCHAR(255) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
    type           VARCHAR(255) NOT NULL,
    minimum_damage DOUBLE PRECISION NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acquired       BOOLEAN DEFAULT FALSE
);
