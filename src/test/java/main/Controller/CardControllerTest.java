package main.Controller;

import Controller.CardController;
import Models.User;
import Server.Request;
import Server.Response;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests der CardController-Logik, die ohne Datenbank auskommen
 * (Validierung, Token-Prüfung, In-Memory-Pakete).
 * Datenbankabhängige Fälle: {@link CardControllerIntegrationTest}.
 */
class CardControllerTest {

    @Test
    void testUserInitialStats() {
        User user = new User("testUser", "password");
        assertEquals(0, user.getWins());
        assertEquals(0, user.getLosses());
        assertEquals(100, user.getElo());
    }

    @Test
    void testUserProfileToJson() {
        User user = new User("testUser", "password");
        user.updateProfile("Test Name", "Test Bio", "test.jpg");
        String json = user.toJson();
        assertTrue(json.contains("Test Name"));
        assertTrue(json.contains("Test Bio"));
    }

    @Test
    void createPackage_shouldReturn403WhenUserIsNotAdmin() {
        CardController cardController = new CardController();

        Response response = cardController.createPackage(createMockRequest("Bearer user-mtcgToken", "[]"));

        assertEquals(403, response.getStatusCode());
        assertEquals("Provided user is not admin", response.getBody());
    }

    @Test
    void acquirePackage_shouldReturn404WhenNoPackagesAvailable() {
        CardController cardController = new CardController();
        Request mockRequest = new Request();
        mockRequest.setHeaders(Map.of("Authorization", "Bearer user-mtcgToken"));

        Response response = cardController.acquirePackage(mockRequest);

        assertEquals(404, response.getStatusCode());
        assertEquals("No packages available", response.getBody());
    }

    @Test
    void getAvailablePackages_shouldReturnZeroInitially() {
        CardController cardController = new CardController();

        Response response = cardController.getAvailablePackages(new Request());

        assertEquals(200, response.getStatusCode());
        assertEquals("Available packages: 0", response.getBody());
    }

    @Test
    void configureDeck_shouldReturn400WhenInvalidCardCount() {
        CardController cardController = new CardController();

        Response response = cardController.configureDeck(
                createMockRequest("Bearer user-mtcgToken", "[\"card1\", \"card2\", \"card3\"]"));

        assertEquals(400, response.getStatusCode());
        assertEquals("A deck must contain exactly 4 cards", response.getBody());
    }

    @Test
    void configureDeck_shouldFailWithLessThanFourCards() {
        CardController cardController = new CardController();

        Response response = cardController.configureDeck(
                createMockRequest("Bearer kienboec-mtcgToken", "[\"" + UUID.randomUUID() + "\"]"));

        assertEquals(400, response.getStatusCode(), "Expected status code 400 for invalid deck size");
        assertEquals("A deck must contain exactly 4 cards", response.getBody());
    }

    @Test
    void configureDeck_shouldFailForInvalidCardIds() {
        CardController cardController = new CardController();

        Response response = cardController.configureDeck(createMockRequest("Bearer kienboec-mtcgToken",
                "[\"non-existent-card-1\", \"non-existent-card-2\", \"non-existent-card-3\", \"non-existent-card-4\"]"));

        assertEquals(400, response.getStatusCode(), "Expected status code 400 for invalid card IDs");
        assertEquals("Invalid card ID format: non-existent-card-1", response.getBody());
    }

    @Test
    void configureDeck_shouldFailForInvalidToken() {
        CardController cardController = new CardController();

        Response response = cardController.configureDeck(createMockRequest("Bearer invalid-token", fourRandomCardIds()));

        assertEquals(401, response.getStatusCode(), "Expected status code 401 for invalid token");
    }

    @Test
    void getDeck_shouldFailWithoutToken() {
        CardController cardController = new CardController();

        Response response = cardController.getDeck(new Request()); // kein Token gesetzt

        assertEquals(401, response.getStatusCode(), "Expected status code 401 for missing token");
        assertEquals("Access token is missing or invalid", response.getBody());
    }

    private Request createMockRequest(String authToken, String body) {
        Request request = new Request();
        request.setHeaders(Map.of("Authorization", authToken));
        request.setBody(body);
        return request;
    }

    private String fourRandomCardIds() {
        return "[\"" + UUID.randomUUID() + "\", \"" + UUID.randomUUID() + "\", \""
                + UUID.randomUUID() + "\", \"" + UUID.randomUUID() + "\"]";
    }
}
