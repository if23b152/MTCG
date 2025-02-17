package Database;
import Models.MonsterCard;
import java.sql.*;
import java.util.*;
import Server.DatabaseConnection;


public class CardRepository {
    private final Connection connection;

    public CardRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize CardRepository", e);
        }
    }
    public void acquirePackage(String username, List<MonsterCard> packageCards) throws SQLException {
        connection.setAutoCommit(false);
        try {
            if (!hasEnoughMoney(username, 5)) {
                throw new IllegalStateException("Not enough money");
            }

            deductCoins(username, 5); //Kauf -cons
            assignCardsToUser(packageCards, username); //+karten

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean hasEnoughMoney(String username, int cost) throws SQLException {
        String query = "SELECT coins FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("coins") >= cost;
                }
            }
        }
        return false;
    }
    public void deductCoins(String username, int amount) throws SQLException {
        String query = "UPDATE users SET coins = coins - ? WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, amount);
            statement.setString(2, username);
            statement.executeUpdate();
        }
    }
    public void assignCardsToUser(List<MonsterCard> cards, String username) throws SQLException {
        String userCardQuery = "INSERT INTO user_cards (user_id, card_id) VALUES ((SELECT username FROM users WHERE username = ?), ?)";
        try (PreparedStatement statement = connection.prepareStatement(userCardQuery)) {
            for (MonsterCard card : cards) {
                statement.setString(1, username);
                statement.setObject(2, card.getId(), java.sql.Types.OTHER);
                statement.executeUpdate();
            }
        }
    }
    public void saveCard(MonsterCard card) throws SQLException {
        String query = "INSERT INTO cards (id, name, damage) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, card.getId(), java.sql.Types.OTHER);
            statement.setString(2, card.getName());
            statement.setDouble(3, card.getDamage());
            statement.executeUpdate();
        }
    }

    public void linkCardToUser(UUID cardId, String username) throws SQLException {
        String userCardQuery = "INSERT INTO user_cards (user_id, card_id) VALUES ((SELECT username FROM users WHERE username = ?), ?)";
        try (PreparedStatement statement = connection.prepareStatement(userCardQuery)) {
            statement.setString(1, username);
            statement.setObject(2, cardId, java.sql.Types.OTHER);
            statement.executeUpdate();
        }
    }

    public void save(MonsterCard card, String username) throws SQLException {
        saveCard(card);
        linkCardToUser(card.getId(), username);
    }



    public List<MonsterCard> findByUsername(String username) throws SQLException {
        String query = "SELECT c.* FROM cards c " +
                "JOIN user_cards uc ON c.id = uc.card_id " +
                "JOIN users u ON u.username = uc.user_id " +
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


    public MonsterCard findCardById(String cardId, String username) throws SQLException {
        String query = "SELECT c.* FROM cards c JOIN user_cards uc ON c.id = uc.card_id WHERE c.id = CAST(? AS UUID) AND uc.user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, cardId);
            statement.setString(2, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new MonsterCard(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getString("name"),
                            resultSet.getFloat("damage")
                    );
                }
            }
        }
        return null;
    }

}