package main.Database;

import main.Models.TradingDeal;
import java.sql.*;
import java.util.*;
import main.Server.DatabaseConnection;
import java.util.UUID;


public class TradingRepository {
    private final Connection connection;

    public TradingRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize TradingRepository", e);
        }
    }

    public void createTradingDeal(TradingDeal deal) throws SQLException {
        String query = "INSERT INTO trading_deals (id, card_to_trade, type, minimum_damage) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, deal.getId().toString());
            statement.setString(2, deal.getCardToTrade().toString());  // UUID zu String konvertieren
            statement.setString(3, deal.getType());
            statement.setDouble(4, deal.getMinimumDamage());
            statement.executeUpdate();
        }
    }


    public List<TradingDeal> getAllTradingDeals() throws SQLException {
        String query = "SELECT * FROM trading_deals";
        List<TradingDeal> deals = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                TradingDeal deal = new TradingDeal(
                        UUID.fromString(resultSet.getString("id")),
                        UUID.fromString(resultSet.getString("card_to_trade")),
                        resultSet.getString("type"),
                        resultSet.getDouble("minimum_damage")
                );
                deals.add(deal);
            }
        }
        return deals;
    }


    public TradingDeal findDealById(String dealId) throws SQLException {
        String query = "SELECT * FROM trading_deals WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, dealId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return new TradingDeal(
                        UUID.fromString(resultSet.getString("id")),
                        UUID.fromString(resultSet.getString("card_to_trade")),
                        resultSet.getString("type"),
                        resultSet.getDouble("minimum_damage")
                );
            }
        }
        return null;
    }

    public void deleteTradingDeal(String dealId) throws SQLException {
        String query = "DELETE FROM trading_deals WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, dealId);
            statement.executeUpdate();
        }
    }
}
