package main.Database;

import main.Models.MonsterCard;
import java.sql.*;
import java.util.*;
import main.Server.DatabaseConnection;


public class CardRepository {
    private final Connection connection;

    public CardRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize CardRepository", e);
        }
    }

    public void save(MonsterCard card, String username) throws SQLException {
        String query = "INSERT INTO cards (id, name, damage) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, card.getId().toString());
            statement.setString(2, card.getName());
            statement.setDouble(3, card.getDamage());
            statement.executeUpdate();

            // Füge Karte zum Benutzer hinzu
            String userCardQuery = "INSERT INTO user_cards (user_id, card_id) VALUES ((SELECT id FROM users WHERE username = ?), ?)";
            try (PreparedStatement userCardStatement = connection.prepareStatement(userCardQuery)) {
                userCardStatement.setString(1, username);
                userCardStatement.setString(2, card.getId().toString());
                userCardStatement.executeUpdate();
            }
        }
    }

    public List<MonsterCard> findByUsername(String username) throws SQLException {
        String query = "SELECT c.* FROM cards c " +
                "JOIN user_cards uc ON c.id = uc.card_id " +
                "JOIN users u ON u.id = uc.user_id " +
                "WHERE u.username = ?";
        List<MonsterCard> cards = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                cards.add(new MonsterCard(
                        UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("name"),
                        resultSet.getDouble("damage")
                ));
            }
        }
        return cards;
    }

    public void delete(String cardId) throws SQLException {
        String query = "DELETE FROM cards WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, cardId);
            statement.executeUpdate();
        }
    }
}
