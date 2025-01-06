package Database;

import Models.MonsterCard;
import java.sql.*;
import java.util.*;
import Server.DatabaseConnection;


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
        deleteDeck(username);

        String query = "INSERT INTO decks (user_id, card_id) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (MonsterCard card : cards) {
                statement.setString(1, username);
                statement.setObject(2, card.getId(), java.sql.Types.OTHER);
                statement.executeUpdate();
            }
        }
    }



    public List<MonsterCard> getDeck(String username) throws SQLException {
        String query = "SELECT c.* FROM cards c " +
                "JOIN decks d ON c.id = d.card_id " +
                "WHERE d.user_id = ?";

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
        String query = "DELETE FROM decks WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

}
