package main.Controller;

import main.Models.MonsterCard;
import main.Server.Request;
import main.Server.Response;
import java.util.*;

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

            packages.add(newPackage);
            return new Response(201, "Package created successfully");
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }

    public Response acquirePackage(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        if (packages.isEmpty()) {
            return new Response(404, "No packages available");
        }

        List<MonsterCard> userCardList = userCards.computeIfAbsent(username, k -> new ArrayList<>());
        if (userCardList.size() >= 20) {
            return new Response(403, "Not enough money");
        }

        List<MonsterCard> acquiredPackage = packages.remove(0);
        userCardList.addAll(acquiredPackage);
        return new Response(200, "Package acquired successfully");
    }

    public Response getCards(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        List<MonsterCard> cards = userCards.get(username);
        if (cards == null || cards.isEmpty()) {
            return new Response(204, "No cards available");
        }

        return new Response(200, cardsToJson(cards));
    }

    public Response getDeck(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        List<MonsterCard> deck = userDecks.get(username);
        if (deck == null || deck.isEmpty()) {
            return new Response(204, "The deck is empty");
        }

        String format = request.getQueryParams().getOrDefault("format", "json");
        if (format.equals("plain")) {
            return new Response(200, deckToPlainText(deck));
        } else {
            return new Response(200, cardsToJson(deck));
        }
    }

    public Response configureDeck(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        try {
            List<String> cardIds = parseCardIds(request.getBody());
            if (cardIds.size() != 4) {
                return new Response(400, "A deck must contain exactly 4 cards");
            }

            List<MonsterCard> newDeck = new ArrayList<>();
            for (String cardId : cardIds) {
                MonsterCard card = findCardById(username, cardId);
                if (card == null) {
                    return new Response(403, "At least one of the provided cards does not belong to the user or is not available");
                }
                newDeck.add(card);
            }

            userDecks.put(username, newDeck);
            return new Response(200, "The deck has been successfully configured");
        } catch (Exception e) {
            return new Response(400, "Invalid request");
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

    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7, token.length() - 9);
        }
        return "";
    }

    private List<MonsterCard> parseCards(String json) {
        List<MonsterCard> cards = new ArrayList<>();
        String[] cardStrings = json.substring(1, json.length() - 1).split("\\},\\{");

        for (String cardString : cardStrings) {
            String cleanCard = cardString.replace("{", "").replace("}", "");
            String[] properties = cleanCard.split(",");

            String id = null;
            String name = null;
            double damage = 0;

            for (String property : properties) {
                String[] keyValue = property.split(":");
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim().replace("\"", "");

                switch (key) {
                    case "Id" -> id = value;
                    case "Name" -> name = value;
                    case "Damage" -> damage = Double.parseDouble(value);
                }
            }

            if (id != null && name != null) {
                cards.add(new MonsterCard(UUID.fromString(id), name, damage));
            }
        }
        return cards;
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
