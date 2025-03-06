package Database;

import Models.TradingDeal;
import Server.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class TradingRepository {
    private final Connection connection;

    public TradingRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize TradingRepository", e);
        }
    }

    // Speichert einen neuen Trading-Deal in der Datenbank
    public void saveTradingDeal(TradingDeal deal, String username) throws SQLException {
        String query = "INSERT INTO trading_deals (id, card_to_trade, user_id, type, minimum_damage) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, deal.getId());        // ID des Trading-Deals
            stmt.setObject(2, deal.getCardToTrade()); // Karte, die getauscht wird
            stmt.setString(3, username);            // Nutzername direkt speichern
            stmt.setString(4, deal.getType());      // Kartentyp (Monster oder Spell)
            stmt.setDouble(5, deal.getMinimumDamage()); // Mindest-Schaden

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println("No rows inserted - possible SQL issue.");
            } else {
                System.out.println("Trading deal saved successfully!");
            }
        } catch (SQLException e) {
            System.err.println("SQL Error in saveTradingDeal: " + e.getMessage());
            throw e;
        }
    }


    public List<TradingDeal> getAllTradingDeals() throws SQLException {
        String query = "SELECT id, card_to_trade, user_id, type, minimum_damage FROM trading_deals";
        List<TradingDeal> tradingDeals = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TradingDeal deal = new TradingDeal(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("card_to_trade")),
                        rs.getString("type"),
                        rs.getDouble("minimum_damage")
                );
                tradingDeals.add(deal);
            }
        }
        return tradingDeals;
    }
    public TradingDeal getTradingDealById(UUID dealId) throws SQLException {
        String query = "SELECT id, card_to_trade, type, minimum_damage FROM trading_deals WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, dealId, java.sql.Types.OTHER);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new TradingDeal(
                            rs.getObject("id", UUID.class), // direkte UUID-Nutzung
                            rs.getObject("card_to_trade", UUID.class), //  vermeidet String-Parsing
                            rs.getString("type"),
                            rs.getDouble("minimum_damage")
                    );
                }
            }
        }
        return null; // Falls kein Deal gefunden wurde
    }

    public String getDealOwner(UUID dealId) throws SQLException {
        String query = "SELECT user_id FROM trading_deals WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, dealId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_id"); // Besitzer des Deals zurückgeben
                }
            }
        }
        return null; // Falls kein Besitzer gefunden wurde
    }
    public void deleteTradingDeal(UUID dealId) throws SQLException {
        String query = "DELETE FROM trading_deals WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setObject(1, dealId);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                System.out.println("No trading deal found with ID: " + dealId);
            } else {
                System.out.println("Trading deal " + dealId + " successfully deleted.");
            }
        }
    }


}