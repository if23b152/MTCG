package main.Controller;

import Controller.CardController;
import Models.User;
import Server.Request;
import Server.Response;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardControllerTest {

    @Test
    void createPackage_shouldReturn201WhenPackageIsValid() {
        CardController cardController = new CardController();
        String validPackageJson = "[" +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Dragon\",\"Damage\":50.0}," +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Knight\",\"Damage\":40.0}," +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Elf\",\"Damage\":30.0}," +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Goblin\",\"Damage\":20.0}," +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Wizard\",\"Damage\":10.0}" +
                "]";

        Request mockRequest = new Request();
        mockRequest.setHeaders(Map.of("Authorization", "Bearer admin-mtcgToken"));
        mockRequest.setBody(validPackageJson);

        Response response = cardController.createPackage(mockRequest);

        assertEquals(201, response.getStatusCode());
        assertEquals("Package created successfully", response.getBody());
    }

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
        String packageJson = "[]";

        Request mockRequest = new Request();
        mockRequest.setHeaders(Map.of("Authorization", "Bearer user-mtcgToken"));
        mockRequest.setBody(packageJson);

        Response response = cardController.createPackage(mockRequest);
        assertEquals(403, response.getStatusCode());
        assertEquals("Provided user is not admin", response.getBody());
    }
    private Request createMockRequest(String authToken, String body) {
        Request request = new Request();
        request.setHeaders(Map.of("Authorization", authToken));
        request.setBody(body);
        return request;
    }
    private String createValidPackageJson() {
        return "[" +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Dragon\",\"Damage\":50.0}," +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Knight\",\"Damage\":40.0}," +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Elf\",\"Damage\":30.0}," +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Goblin\",\"Damage\":20.0}," +
                "{\"Id\":\"" + UUID.randomUUID() + "\",\"Name\":\"Wizard\",\"Damage\":10.0}" +
                "]";
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
    void getAvailablePackages_shouldReturnCorrectCount() {
        CardController cardController = new CardController();
        cardController.createPackage(createMockRequest("Bearer admin-mtcgToken", createValidPackageJson()));
        cardController.createPackage(createMockRequest("Bearer admin-mtcgToken", createValidPackageJson()));

        Request mockRequest = new Request();

        Response response = cardController.getAvailablePackages(mockRequest);

        assertEquals(200, response.getStatusCode());
        assertEquals("Available packages: 2", response.getBody());
    }
    @Test
    void configureDeck_shouldReturn400WhenInvalidCardCount() {
        CardController cardController = new CardController();
        String deckJson = "[\"card1\", \"card2\", \"card3\"]"; // Nur 3 Karten

        Request mockRequest = new Request();
        mockRequest.setHeaders(Map.of("Authorization", "Bearer user-mtcgToken"));
        mockRequest.setBody(deckJson);


        Response response = cardController.configureDeck(mockRequest);

        assertEquals(400, response.getStatusCode());
        assertEquals("A deck must contain exactly 4 cards", response.getBody());
    }
    @Test
    void configureDeck_shouldSucceed() {
        CardController cardController = new CardController();
        String token = "Bearer kienboec-mtcgToken";

        Request configureDeckRequest = createMockRequest(token,
                "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", " +
                        "\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]");
        Response configureResponse = cardController.configureDeck(configureDeckRequest);

        assertEquals(200, configureResponse.getStatusCode(), "Deck configuration should succeed");
    }

    @Test
    void getDeck_shouldReturn200() {
        CardController cardController = new CardController();
        String token = "Bearer kienboec-mtcgToken";

        Request configureDeckRequest = createMockRequest(token,
                "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", " +
                        "\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]");
        cardController.configureDeck(configureDeckRequest);

        Request mockRequest = new Request();
        mockRequest.setHeaders(Map.of("Authorization", token));
        Response response = cardController.getDeck(mockRequest);

        assertEquals(200, response.getStatusCode(), "Expected status code 200 for getDeck");
    }

    @Test
    void getDeck_shouldContainConfiguredCards() {
        CardController cardController = new CardController();
        String token = "Bearer kienboec-mtcgToken";

        Request configureDeckRequest = createMockRequest(token,
                "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", " +
                        "\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]");
        cardController.configureDeck(configureDeckRequest);

        Request mockRequest = new Request();
        mockRequest.setHeaders(Map.of("Authorization", token));
        Response response = cardController.getDeck(mockRequest);
        String responseBody = response.getBody();

        assertTrue(responseBody.contains("845f0dc7-37d0-426e-994e-43fc3ac83c08"),
                "Deck should contain the card with ID 845f0dc7-37d0-426e-994e-43fc3ac83c08");
        assertTrue(responseBody.contains("99f8f8dc-e25e-4a95-aa2c-782823f36e2a"),
                "Deck should contain the card with ID 99f8f8dc-e25e-4a95-aa2c-782823f36e2a");
        assertTrue(responseBody.contains("e85e3976-7c86-4d06-9a80-641c2019a79f"),
                "Deck should contain the card with ID e85e3976-7c86-4d06-9a80-641c2019a79f");
        assertTrue(responseBody.contains("171f6076-4eb5-4a7d-b3f2-2d650cc3d237"),
                "Deck should contain the card with ID 171f6076-4eb5-4a7d-b3f2-2d650cc3d237");
    }
    @Test
    void configureDeck_shouldFailForInvalidToken() {
        CardController cardController = new CardController();
        String invalidToken = "Bearer invalid-token";

        Request configureDeckRequest = createMockRequest(invalidToken,
                "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\", \"99f8f8dc-e25e-4a95-aa2c-782823f36e2a\", " +
                        "\"e85e3976-7c86-4d06-9a80-641c2019a79f\", \"171f6076-4eb5-4a7d-b3f2-2d650cc3d237\"]");
        Response configureResponse = cardController.configureDeck(configureDeckRequest);

        assertEquals(403, configureResponse.getStatusCode(), "Expected status code 403 for invalid token");
    }
    @Test
    void getDeck_shouldFailWithoutToken() {
        CardController cardController = new CardController();

        Request mockRequest = new Request(); // Kein Token gesetzt
        Response response = cardController.getDeck(mockRequest);

        assertEquals(401, response.getStatusCode(), "Expected status code 401 for missing token");
        assertEquals("Authorization header must start with 'Bearer '", response.getBody(), "Expected 'Unauthorized' message for missing token");
    }
    @Test
    void getDeck_shouldReturnEmptyWhenNoDeckConfigured() {

        CardController cardController = new CardController();
        String token = "Bearer admin-mtcgToken";

        Request mockRequest = new Request();
        mockRequest.setHeaders(Map.of("Authorization", token));

        Response response = cardController.getDeck(mockRequest);

        assertEquals(204, response.getStatusCode(), "Expected status code 204 for empty deck");
        assertEquals("The deck is empty", response.getBody(), "Expected 'The deck is empty' message");
    }


    @Test
    void configureDeck_shouldFailWithLessThanFourCards() {
        CardController cardController = new CardController();
        String token = "Bearer kienboec-mtcgToken";

        Request configureDeckRequest = createMockRequest(token,
                "[\"845f0dc7-37d0-426e-994e-43fc3ac83c08\"]"); // Nur eine Karte
        Response configureResponse = cardController.configureDeck(configureDeckRequest);

        assertEquals(400, configureResponse.getStatusCode(), "Expected status code 400 for invalid deck size");
        assertEquals("A deck must contain exactly 4 cards", configureResponse.getBody(),
                "Expected 'A deck must contain exactly 4 cards' message");
    }
    @Test
    void getDeck_shouldFailForUnlinkedCards() {
        CardController cardController = new CardController();
        String token = "Bearer kienboec-mtcgToken";

        Request configureDeckRequest = createMockRequest(token,
                "[\"non-existent-card-1\", \"non-existent-card-2\", \"non-existent-card-3\", \"non-existent-card-4\"]");
        Response configureResponse = cardController.configureDeck(configureDeckRequest);

        assertEquals(400, configureResponse.getStatusCode(), "Expected status code 400 for invalid card IDs");
        assertEquals("Invalid card ID format: non-existent-card-1", configureResponse.getBody(),
                "Expected error message for invalid card ID");
    }


}
