package main.Controller;

import Controller.CardController;
import Server.DatabaseConnection;
import Server.Request;
import Server.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integrationstests gegen eine echte PostgreSQL-Datenbank (Schema aus init.sql).
 * Jeder Lauf legt eigene Testbenutzer und -karten an und räumt sie danach wieder auf.
 * Ist keine Datenbank erreichbar, werden die Tests übersprungen statt fehlzuschlagen.
 * Verbindung über MTCG_DB_URL / MTCG_DB_USER / MTCG_DB_PASSWORD.
 */
class CardControllerIntegrationTest {

    private static final String ADMIN = "admin";
    private static final String PLAYER = "it_" + UUID.randomUUID().toString().substring(0, 8);
    private static final String EMPTY_PLAYER = PLAYER + "_empty";

    private static final List<UUID> playerCards = new ArrayList<>();
    private static final List<UUID> createdCards = new ArrayList<>();
    private static boolean adminCreatedByTest;
    private static boolean seeded;

    @BeforeAll
    static void seedDatabase() throws SQLException {
        Connection connection;
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            assumeTrue(false, "Keine Datenbank erreichbar – Integrationstests übersprungen: " + e.getMessage());
            return;
        }

        adminCreatedByTest = insertUser(connection, ADMIN);
        insertUser(connection, PLAYER);
        insertUser(connection, EMPTY_PLAYER);

        String[] names = {"WaterGoblin", "FireElf", "Dragon", "Knight"};
        for (int i = 0; i < names.length; i++) {
            UUID id = UUID.randomUUID();
            execute(connection, "INSERT INTO cards (id, name, damage) VALUES (?, ?, ?)", id, names[i], 10.0 + i * 10);
            execute(connection, "INSERT INTO user_cards (user_id, card_id) VALUES (?, ?)", PLAYER, id);
            playerCards.add(id);
        }
        createdCards.addAll(playerCards);
        seeded = true;
    }

    @AfterAll
    static void cleanUp() throws SQLException {
        if (!seeded) {
            return;
        }
        Connection connection = DatabaseConnection.getConnection();
        for (UUID id : createdCards) {
            execute(connection, "DELETE FROM decks WHERE card_id = ?", id);
            execute(connection, "DELETE FROM user_cards WHERE card_id = ?", id);
            execute(connection, "DELETE FROM cards WHERE id = ?", id);
        }
        execute(connection, "DELETE FROM users WHERE username IN (?, ?)", PLAYER, EMPTY_PLAYER);
        if (adminCreatedByTest) {
            execute(connection, "DELETE FROM users WHERE username = ?", ADMIN);
        }
    }

    @Test
    void createPackage_shouldReturn201WhenPackageIsValid() {
        CardController cardController = new CardController();
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID());
        createdCards.addAll(ids);
        String json = "[" +
                "{\"Id\":\"" + ids.get(0) + "\",\"Name\":\"Dragon\",\"Damage\":50.0}," +
                "{\"Id\":\"" + ids.get(1) + "\",\"Name\":\"Knight\",\"Damage\":40.0}," +
                "{\"Id\":\"" + ids.get(2) + "\",\"Name\":\"Elf\",\"Damage\":30.0}," +
                "{\"Id\":\"" + ids.get(3) + "\",\"Name\":\"Goblin\",\"Damage\":20.0}," +
                "{\"Id\":\"" + ids.get(4) + "\",\"Name\":\"Wizard\",\"Damage\":10.0}" +
                "]";

        Response response = cardController.createPackage(request("Bearer " + ADMIN + "-mtcgToken", json));

        assertEquals(201, response.getStatusCode());
        assertEquals("Package created successfully", response.getBody());
    }

    @Test
    void configureDeck_shouldSucceedAndDeckShouldContainCards() {
        CardController cardController = new CardController();
        String token = "Bearer " + PLAYER + "-mtcgToken";

        Response configure = cardController.configureDeck(request(token, deckJson(playerCards)));
        assertEquals(200, configure.getStatusCode(), "Deck configuration should succeed");

        Response deck = cardController.getDeck(request(token, null));
        assertEquals(200, deck.getStatusCode(), "Expected status code 200 for getDeck");
        for (UUID id : playerCards) {
            assertTrue(deck.getBody().contains(id.toString()), "Deck should contain card " + id);
        }
    }

    @Test
    void configureDeck_shouldReturn403ForCardsOfAnotherUser() {
        CardController cardController = new CardController();

        Response response = cardController.configureDeck(
                request("Bearer " + EMPTY_PLAYER + "-mtcgToken", deckJson(playerCards)));

        assertEquals(403, response.getStatusCode());
    }

    @Test
    void getDeck_shouldReturn204WhenNoDeckConfigured() {
        CardController cardController = new CardController();

        Response response = cardController.getDeck(request("Bearer " + EMPTY_PLAYER + "-mtcgToken", null));

        assertEquals(204, response.getStatusCode(), "Expected status code 204 for empty deck");
        assertEquals("The deck is empty", response.getBody());
    }

    private static String deckJson(List<UUID> ids) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < ids.size(); i++) {
            json.append(i > 0 ? ", " : "").append('"').append(ids.get(i)).append('"');
        }
        return json.append(']').toString();
    }

    private static Request request(String authToken, String body) {
        Request request = new Request();
        request.setHeaders(Map.of("Authorization", authToken));
        if (body != null) {
            request.setBody(body);
        }
        return request;
    }

    /** @return true, wenn der Benutzer neu angelegt wurde */
    private static boolean insertUser(Connection connection, String username) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO users (username, password) VALUES (?, ?) ON CONFLICT (username) DO NOTHING")) {
            statement.setString(1, username);
            statement.setString(2, "test-password");
            return statement.executeUpdate() == 1;
        }
    }

    private static void execute(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof UUID) {
                    statement.setObject(i + 1, params[i], java.sql.Types.OTHER);
                } else {
                    statement.setObject(i + 1, params[i]);
                }
            }
            statement.executeUpdate();
        }
    }
}
