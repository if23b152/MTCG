package Controller;

import Models.User;
import Server.Request;
import Server.Response;
import Database.UserRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StatsController {
    private final UserRepository userRepository;

    public StatsController(Map<String, User> users) {
        this.userRepository = new UserRepository();
    }

    public Response getStats(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        try {
            System.out.println("Fetching stats for username: " + username);
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return new Response(401, "Unauthorized");
            }

            String stats = String.format("{\"Name\":\"%s\",\"Elo\":%d,\"Wins\":%d,\"Losses\":%d}",
                    user.getName(), user.getElo(), user.getWins(), user.getLosses());
            return new Response(200, stats);
        } catch (SQLException e) {
            return new Response(500, "Error retrieving user stats: " + e.getMessage());
        }
    }

    public Response getScoreboard(Request request) {
        try {
            List<User> sortedUsers = userRepository.getAllUsers();

            StringBuilder scoreboard = new StringBuilder("[");
            for (int i = 0; i < sortedUsers.size(); i++) {
                User user = sortedUsers.get(i);
                scoreboard.append(String.format(
                        "{\"Name\":\"%s\",\"Elo\":%d,\"Wins\":%d,\"Losses\":%d}",
                        user.getName(), user.getElo(), user.getWins(), user.getLosses()));
                if (i < sortedUsers.size() - 1) {
                    scoreboard.append(",");
                }
            }
            scoreboard.append("]");

            return new Response(200, scoreboard.toString());
        } catch (SQLException e) {
            return new Response(500, "Error retrieving scoreboard: " + e.getMessage());
        }
    }


    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String[] parts = token.substring(7).split("-");
            if (parts.length == 2 && parts[1].equals("mtcgToken")) {
                return parts[0];
            }
        }
        throw new IllegalArgumentException("Invalid token format");
    }
}