package main.Database;

import main.Models.MonsterCard;
import java.sql.*;
import java.util.*;
import main.Server.DatabaseConnection;


public class DeckRepository {
    private final Connection connection;

    public DeckRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize DeckRepository", e);
        }
    }

    public void saveDeck(String username, List<MonsterCard> cards) throws SQLException {
        // Zuerst altes Deck löschen
        deleteDeck(username);

        String query = "INSERT INTO decks (user_id, card_id) " +
                "VALUES ((SELECT id FROM users WHERE username = ?), ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (MonsterCard card : cards) {
                statement.setString(1, username);
                statement.setString(2, card.getId().toString());
                statement.executeUpdate();
            }
        }
    }

    public List<MonsterCard> getDeck(String username) throws SQLException {
        String query = "SELECT c.* FROM cards c " +
                "JOIN decks d ON c.id = d.card_id " +
                "JOIN users u ON u.id = d.user_id " +
                "WHERE u.username = ?";

        List<MonsterCard> deck = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                deck.add(new MonsterCard(
                        UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("name"),
                        resultSet.getDouble("damage")
                ));
            }
        }
        return deck;
    }

    public void deleteDeck(String username) throws SQLException {
        String query = "DELETE FROM decks WHERE user_id = (SELECT id FROM users WHERE username = ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

    public boolean isDeckComplete(String username) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM decks d " +
                "JOIN users u ON u.id = d.user_id " +
                "WHERE u.username = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("count") == 4;
            }
        }
        return false;
    }
}
