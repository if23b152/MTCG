package main.Database;

import main.Models.User;
import main.Server.DatabaseConnection;
import java.sql.*;
import main.Database.UserRepository;


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
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                User user = new User(
                        resultSet.getString("username"),
                        resultSet.getString("password")
                );
                user.setName(resultSet.getString("name"));
                user.setBio(resultSet.getString("bio"));
                user.setImage(resultSet.getString("image"));
                return user;
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
        String query = "UPDATE users SET password = ?, coins = ?, name = ?, bio = ?, " +
                "image = ?, elo = ?, wins = ?, losses = ? WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getPassword());
            statement.setInt(2, user.getCoins());
            statement.setString(3, user.getName());
            statement.setString(4, user.getBio());
            statement.setString(5, user.getImage());
            statement.setInt(6, user.getElo());
            statement.setInt(7, user.getWins());
            statement.setInt(8, user.getLosses());
            statement.setString(9, user.getUsername());
            statement.executeUpdate();
        }
    }

    public void delete(String username) throws SQLException {
        String query = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }
}
