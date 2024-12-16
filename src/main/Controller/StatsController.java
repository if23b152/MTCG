package main.Controller;

import main.Models.User;
import main.Server.Request;
import main.Server.Response;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class StatsController {
    private final Map<String, User> users;

    public StatsController(Map<String, User> users) {
        this.users = users;
    }

    public Response getStats(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);
        User user = users.get(username);

        if (user == null) {
            return new Response(401, "Unauthorized");
        }

        String stats = String.format("{\"Name\":\"%s\",\"Elo\":%d,\"Wins\":%d,\"Losses\":%d}",
                user.getName(), user.getElo(), user.getWins(), user.getLosses());
        return new Response(200, stats);
    }

    public Response getScoreboard(Request request) {
        List<User> sortedUsers = users.values().stream()
                .sorted((u1, u2) -> Integer.compare(u2.getElo(), u1.getElo()))
                .collect(Collectors.toList());

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
    }

    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7, token.length() - 9);
        }
        return "";
    }
}
