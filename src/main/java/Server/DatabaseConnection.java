package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Credentials can be overridden via environment variables (defaults for local development)
    private static final String URL = env("MTCG_DB_URL", "jdbc:postgresql://localhost:5432/mtcg");
    private static final String USER = env("MTCG_DB_USER", "postgres");
    private static final String PASSWORD = env("MTCG_DB_PASSWORD", "0000");

    private static Connection connection;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
