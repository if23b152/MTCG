package Database;

import Models.MonsterCard;
import Models.User;
import Server.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class UserRepository {
    private final Connection connection;


    public UserRepository() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize UserRepository", e);
        }
    }

    public User findByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(
                            resultSet.getString("username"),
                            resultSet.getString("password"),
                            resultSet.getInt("coins"),
                            resultSet.getString("name"),
                            resultSet.getString("bio"),
                            resultSet.getString("image"),
                            resultSet.getInt("elo"),
                            resultSet.getInt("wins"),
                            resultSet.getInt("losses")
                    );
                    List<MonsterCard> deck = getDeckByUsername(username);
                    user.setDeck(deck);
                    return user;
                }
            }
        }
        return null;
    }




    public void save(User user) throws SQLException {
        String query = "INSERT INTO users (username, password, coins, name, bio, image, elo, wins, losses) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setInt(3, user.getCoins());
            statement.setString(4, user.getName());
            statement.setString(5, user.getBio());
            statement.setString(6, user.getImage());
            statement.setInt(7, user.getElo());
            statement.setInt(8, user.getWins());
            statement.setInt(9, user.getLosses());
            statement.executeUpdate();
        }
    }

    public void update(User user) throws SQLException {
        String query = "UPDATE users SET name = ?, bio = ?, image = ?, elo = ?, wins = ?, losses = ? WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getBio());
            statement.setString(3, user.getImage());
            statement.setInt(4, user.getElo());
            statement.setInt(5, user.getWins());
            statement.setInt(6, user.getLosses());
            statement.setString(7, user.getUsername());
            int rowsAffected = statement.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
        }
    }

    public List<User> getAllUsers() throws SQLException {
        String query = "SELECT * FROM users ORDER BY elo DESC";
        List<User> users = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                users.add(new User(
                        resultSet.getString("username"),
                        resultSet.getString("password"),
                        resultSet.getInt("coins"),
                        resultSet.getString("name"),
                        resultSet.getString("bio"),
                        resultSet.getString("image"),
                        resultSet.getInt("elo"),
                        resultSet.getInt("wins"),
                        resultSet.getInt("losses")
                ));
            }
        }
        return users;
    }

    public List<MonsterCard> getDeckByUsername(String username) throws SQLException {
        List<MonsterCard> deck = new ArrayList<>();
        String query = "SELECT c.id, c.name, c.damage FROM decks d " +
                "JOIN cards c ON d.card_id = c.id " +
                "WHERE d.user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MonsterCard card = new MonsterCard(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getString("name"),
                            resultSet.getDouble("damage")
                    );
                    deck.add(card);
                }
            }
        }
        return deck;
    }

}
