package Controller;

import Database.CardRepository;
import Database.TradingRepository;
import Models.MonsterCard;
import Models.TradingDeal;
import Server.DatabaseConnection;
import Server.Request;
import Server.Response;
//import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class TradingController {
   // private final Map<String, TradingDeal> tradingDeals = new HashMap<>();
    private final CardController cardController;
    private final CardRepository cardRepository;
    private final TradingRepository tradingRepository;


    public TradingController(CardController cardController, TradingRepository tradingRepository) {
        this.cardController = cardController;
        this.tradingRepository = tradingRepository;
        this.cardRepository = new CardRepository();
    }

    public Response getTradingDeals(Request request) {
        try {
            List<TradingDeal> tradingDeals = tradingRepository.getAllTradingDeals(); // trading- Deals aus DB abrufen

            if (tradingDeals.isEmpty()) {
                return new Response(204, "No trading deals available"); // keine Deals vorhanden
            }

            return new Response(200, tradingDealsToJson(tradingDeals)); // deals als JSON zurückgeben
        } catch (SQLException e) {
            return new Response(500, "Database error: " + e.getMessage()); // datenbankfehler behandeln
        }
    }

//    public Response createTradingDealWithCard(Request request, String dealId, UUID cardToTradeId) {
//        String token = request.getHeaders().get("Authorization");
//        String username = extractUsername(token);
//
//        try {
//            // Überprüfe, ob die Karten-ID in der Datenbank existiert
//            CardRepository cardRepository = new CardRepository();
//            MonsterCard card = cardRepository.findCardById(cardToTradeId.toString(), username);
//            if (card == null) {
//                return new Response(400, "Card ID not found");
//            }
//
//            // Erstelle einen neuen TradingDeal mit Standardwerten
//            TradingDeal deal = new TradingDeal(
//                    UUID.fromString(dealId), // Verwende die übergebene Deal-ID
//                    cardToTradeId,
//                    "monster", // Standard-Typ
//                    0.0 // Standard-Schaden
//            );
//
//            tradingRepository.saveTradingDeal(deal, username);
//            return new Response(201, "Trading deal successfully created");
//        } catch (SQLException e) {
//            return new Response(500, "Database error: " + e.getMessage());
//        } catch (Exception e) {
//            return new Response(400, "Invalid request");
//        }
//    }


    public Response createTradingDeal(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(request.getBody());
            UUID dealId = UUID.fromString(jsonNode.get("Id").asText());
            UUID cardToTradeId = UUID.fromString(jsonNode.get("CardToTrade").asText());
            String type = jsonNode.get("Type").asText();
            double minimumDamage = jsonNode.get("MinimumDamage").asDouble();
            if (cardRepository.isCardInDeck(username, cardToTradeId)) {
                return new Response(403, "Card is in the deck and cannot be traded.");
            }
            TradingDeal deal = new TradingDeal(dealId, cardToTradeId, type, minimumDamage);
            tradingRepository.saveTradingDeal(deal, username);

            return new Response(201, "Trading deal successfully created");
        } catch (IllegalArgumentException e) {
            return new Response(400, "Invalid card ID format");
        } catch (SQLException e) {
            return new Response(500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            return new Response(400, "Invalid request");
        }
    }



    public Response deleteTradingDeal(Request request, String dealId) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        try {
            TradingDeal deal = tradingRepository.getTradingDealById(UUID.fromString(dealId));
            if (deal == null) {
                return new Response(404, "Trading deal not found");
            }

            String owner = tradingRepository.getDealOwner(UUID.fromString(dealId));
            if (!owner.equals(username)) {
                return new Response(403, "You can only delete your own trading deals.");
            }

            tradingRepository.deleteTradingDeal(UUID.fromString(dealId));
            return new Response(200, "Trading deal successfully deleted");
        } catch (SQLException e) {
            return new Response(500, "Database error: " + e.getMessage());
        }
    }


    public Response trade(Request request, String tradingDealId) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);
        System.out.println("DEBUG: TradingDealId = " + tradingDealId);
        System.out.println("DEBUG: Username = " + username);
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);
            TradingDeal deal = tradingRepository.getTradingDealById(UUID.fromString(tradingDealId));
            if (deal == null) {
                return new Response(404, "Trading deal not found");
            }

            String owner = cardRepository.getCardOwner(deal.getCardToTrade());
            if (owner.equals(username)) {
                return new Response(403, "Trading with yourself is not allowed.");
            }

            String offeredCardId = request.getBody().replaceAll("\"", "");
            MonsterCard offeredCard = cardRepository.findCardById(offeredCardId, username);
            System.out.println("User " + username + " is offering card: " + offeredCardId);
            System.out.println("Offered card found: " + (offeredCard != null ? offeredCard.getName() : "NOT FOUND"));
            if (cardRepository.doesUserOwnCard(username, deal.getCardToTrade())) {
                return new Response(403, "Trading with yourself is not allowed.");
            }

            String deleteOldOwnersQuery = "DELETE FROM user_cards WHERE card_id IN (?, ?)";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteOldOwnersQuery)) {
                deleteStmt.setObject(1, deal.getCardToTrade()); // alte Trading karte
                deleteStmt.setObject(2, UUID.fromString(offeredCardId)); // angebotene krte
                deleteStmt.executeUpdate();
            }

            // *2. Füge neue Besitzer in user_cards ein*
            String insertNewOwnersQuery = "INSERT INTO user_cards (user_id, card_id) VALUES (?, ?), (?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertNewOwnersQuery)) {
                insertStmt.setString(1, username); // neuer besitzer für die alte trading-Karte
                insertStmt.setObject(2, deal.getCardToTrade());

                insertStmt.setString(3, owner); // alter besitzer bekommt die angebotene Karte
                insertStmt.setObject(4, UUID.fromString(offeredCardId));

                insertStmt.executeUpdate();
            }

            // *3. Lösche den Trading-Deal*
            String deleteQuery = "DELETE FROM trading_deals WHERE id = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.setObject(1, UUID.fromString(tradingDealId));
                deleteStmt.executeUpdate();
            }

            // *4. Commit der Transaktion*
            connection.commit();
            return new Response(200, "Trading deal successfully executed");
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback(); // falls fehler alles rückgängig machen
                } catch (SQLException rollbackError) {
                    return new Response(500, "Rollback failed: " + rollbackError.getMessage());
                }
            }
            return new Response(500, "Trade failed: " + e.getMessage());
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // autoCommit wieder aktiv
                } catch (SQLException ignored) {
                }
            }
        }
    }



    private boolean isTradeValid(TradingDeal deal, MonsterCard offeredCard) {
        return offeredCard.getDamage() >= deal.getMinimumDamage() &&
                offeredCard.getType().equalsIgnoreCase(deal.getType());
    }


    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7).split("-")[0]; // bnutzername extrahiert wird
        }
        return "";
    }



    private String tradingDealsToJson(Collection<TradingDeal> deals) {
        StringBuilder json = new StringBuilder("[");
        Iterator<TradingDeal> iterator = deals.iterator();
        while (iterator.hasNext()) {
            TradingDeal deal = iterator.next();
            json.append(String.format(
                    "{\"Id\":\"%s\",\"CardToTrade\":\"%s\",\"Type\":\"%s\",\"MinimumDamage\":%.1f}",
                    deal.getId().toString(),
                    deal.getCardToTrade(),
                    deal.getType(),
                    deal.getMinimumDamage()
            ));
            if (iterator.hasNext()) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

}