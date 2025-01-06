package Controller;

import Database.CardRepository;
import Database.DeckRepository;
import Models.MonsterCard;
import Server.Request;
import Server.Response;

import java.sql.SQLException;
import java.util.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
public class CardController {
    private final Map<String, List<MonsterCard>> userCards = new HashMap<>();
    private final Map<String, List<MonsterCard>> userDecks = new HashMap<>();
    private final List<List<MonsterCard>> packages = new ArrayList<>();

    public Response createPackage(Request request) {
        try {
            if (!isAdmin(request.getHeaders().get("Authorization"))) {
                return new Response(403, "Provided user is not admin");
            }

            List<MonsterCard> newPackage = parseCards(request.getBody());
            if (newPackage.size() != 5) {
                return new Response(400, "Package must contain exactly 5 cards");
            }
            System.out.println("Packages before adding: " + packages.size());
            // memory-Liste
            packages.add(newPackage);
            // datenbank
            CardRepository cardRepository = new CardRepository();
            String adminUsername = extractUsernameFromToken(request.getHeaders().get("Authorization"));

            for (MonsterCard card : newPackage) {
                cardRepository.save(card, adminUsername); // jede Karte speichern u
            }


            return new Response(201, "Package created successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(400, "Invalid request");
        }
    }


    private String extractUsernameFromToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header must start with 'Bearer '");
        }

        String token = authorizationHeader.substring(7);
        if (!token.contains("-")) {
            throw new UnauthorizedException("Token is missing a '-' separator");
        }

        return token.split("-")[0];
    }

    public class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }



    public Response acquirePackage(Request request) {
        try {
            String token = request.getHeaders().get("Authorization");
            String username = extractUsernameFromToken(token);

            if (packages.isEmpty()) {
                return new Response(404, "No packages available");
            }
            System.out.println("Available packages before acquiring: " + packages.size());

            List<MonsterCard> acquiredPackage = packages.get(0);


            CardRepository cardRepository = new CardRepository();
            cardRepository.acquirePackage(username, acquiredPackage);
            packages.remove(0);
            System.out.println("Available packages after acquiring: " + packages.size());
            return new Response(201, "Package acquired successfully");
        } catch (IllegalStateException e) {
            return new Response(403, e.getMessage()); // Nicht genug geld
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(500, "Internal server error: " + e.getMessage());
        }
    }


    public Response getAvailablePackages(Request request) {
        return new Response(200, "Available packages: " + packages.size());
    }




    public Response getCards(Request request) {
        try {
            String token = request.getHeaders().get("Authorization");
            String username = extractUsernameFromToken(token);

            CardRepository cardRepository = new CardRepository();
            List<MonsterCard> cards = cardRepository.findByUsername(username);

            if (cards == null || cards.isEmpty()) {
                return new Response(204, "No cards available");
            }

            return new Response(200, cardsToJson(cards));
        } catch (UnauthorizedException e) {
            return new Response(401, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(500, "Internal server error: " + e.getMessage());
        }
    }



    public Response getDeck(Request request) {
        try {
            String token = request.getHeaders().get("Authorization");
            String username = extractUsernameFromToken(token);

            DeckRepository deckRepository = new DeckRepository();
            List<MonsterCard> deck = deckRepository.getDeck(username);

            if (deck == null || deck.isEmpty()) {
                return new Response(204, "The deck is empty");
            }

            String format = request.getQueryParams().getOrDefault("format", "json");
            if (format.equals("plain")) {
                return new Response(200, deckToPlainText(deck));
            } else {
                return new Response(200, cardsToJson(deck));
            }
        } catch (UnauthorizedException e) {
            return new Response(401, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(500, "Internal server error: " + e.getMessage());
        }
    }


    public Response configureDeck(Request request) {
        try {
            String token = request.getHeaders().get("Authorization");
            String username = extractUsernameFromToken(token);

            List<String> cardIds = parseCardIds(request.getBody());
            if (cardIds.size() != 4) {
                return new Response(400, "A deck must contain exactly 4 cards");
            }

            CardRepository cardRepository = new CardRepository();
            DeckRepository deckRepository = new DeckRepository();
            List<MonsterCard> newDeck = new ArrayList<>();
            for (String cardId : cardIds) {

                if (!isValidUUID(cardId)) {
                    return new Response(400, "Invalid card ID format: " + cardId);
                }

                MonsterCard card = cardRepository.findCardById(cardId, username);
                if (card == null) {
                    return new Response(403, "At least one of the provided cards does not belong to the user or is not available");
                }
                newDeck.add(card);
            }
            deckRepository.saveDeck(username, newDeck);
            return new Response(200, "The deck has been successfully configured");
        } catch (UnauthorizedException e) {
            return new Response(401, e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(500, "Internal server error: " + e.getMessage());
        }
    }

    private boolean isValidUUID(String cardId) {
        try {
            UUID.fromString(cardId);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public void exchangeCards(String user1, String user2, String card1Id, String card2Id) {
        List<MonsterCard> user1Cards = userCards.get(user1);
        List<MonsterCard> user2Cards = userCards.get(user2);

        MonsterCard card1 = findCardById(user1, card1Id);
        MonsterCard card2 = findCardById(user2, card2Id);

        if (card1 != null && card2 != null) {
            user1Cards.remove(card1);
            user2Cards.remove(card2);
            user1Cards.add(card2);
            user2Cards.add(card1);
        }
    }

    private boolean isAdmin(String token) {
        return token != null && token.equals("Bearer admin-mtcgToken");
    }


    private List<MonsterCard> parseCards(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json, new TypeReference<List<MonsterCard>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error parsing cards", e);
        }
    }


    private List<String> parseCardIds(String body) {
        List<String> cardIds = new ArrayList<>();
        try {
            String cleanBody = body.trim().substring(1, body.length() - 1);
            String[] ids = cleanBody.split(",");

            for (String id : ids) {
                String cleanId = id.trim().replace("\"", "");
                cardIds.add(cleanId);
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return cardIds;
    }

    public MonsterCard findCardById(String username, String cardId) {
        List<MonsterCard> userCardList = userCards.get(username);
        if (userCardList != null) {
            return userCardList.stream()
                    .filter(card -> card.getId().toString().equals(cardId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private String cardsToJson(List<MonsterCard> cards) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < cards.size(); i++) {
            MonsterCard card = cards.get(i);
            json.append(String.format(
                    "{\"Id\":\"%s\",\"Name\":\"%s\",\"Damage\":%.1f}",
                    card.getId(),
                    card.getName(),
                    card.getDamage()
            ));
            if (i < cards.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private String deckToPlainText(List<MonsterCard> deck) {
        StringBuilder plainText = new StringBuilder();
        for (MonsterCard card : deck) {
            plainText.append(card.getName())
                    .append(": ")
                    .append(card.getDamage())
                    .append(" damage\n");
        }
        return plainText.toString();
    }
}
