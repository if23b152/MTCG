##Design und Struktur 
 MVC Pattern für klare Trennung der Komponenten 
 Models (User, Card, Deck, etc.) 
 Controller (UserController, CardController, etc.) 
 Repository Layer für Datenbankzugriffe 
 Repository Pattern für Datenbankzugriffe 
 RESTful API Design nach OpenAPI Spezifikation 
 
##Tabellenübersicht 
CREATE TABLE users ( 
    id SERIAL PRIMARY KEY, 
    username VARCHAR(255) UNIQUE, 
    password VARCHAR(255), 
    coins INTEGER DEFAULT 20, 
    elo INTEGER DEFAULT 100, 
    wins INTEGER DEFAULT 0, 
    losses INTEGER DEFAULT 0, 
    name VARCHAR(255), 
    bio TEXT, 
    image VARCHAR(255) 
); 
 
CREATE TABLE packages ( 
    id SERIAL PRIMARY KEY, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    acquired BOOLEAN DEFAULT FALSE 
); 
 
CREATE TABLE cards ( 
    id UUID PRIMARY KEY, 
    name VARCHAR(255), 
    damage FLOAT, 
    element_type VARCHAR(50), 
    card_type VARCHAR(50), 
    package_id INTEGER REFERENCES packages(id) 
); 
 
CREATE TABLE user_cards ( 
    user_id INTEGER REFERENCES users(id), 
    card_id UUID REFERENCES cards(id), 
    PRIMARY KEY (user_id, card_id) 
); 
 
CREATE TABLE decks ( 
    user_id INTEGER REFERENCES users(id), 
    card_id UUID REFERENCES cards(id), 
    PRIMARY KEY (user_id, card_id) 
); 
 
CREATE TABLE trading_deals ( 
    id UUID PRIMARY KEY, 
    card_to_trade UUID REFERENCES cards(id), 
    user_id INTEGER REFERENCES users(id), 
    type VARCHAR(50), 
    minimum_damage FLOAT 
);
