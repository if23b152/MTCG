package Controller;

import Database.UserRepository;
import Models.MonsterCard;
import Models.User;
import Models.Battle;
import Server.Request;
import Server.Response;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BattleController {
    private final Map<String, User> users;
    private final ConcurrentLinkedQueue<String> battleQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, Long> queueTimestamps = new ConcurrentHashMap<>();
    private final UserRepository userRepository;

    public BattleController(Map<String, User> users) {
        this.users = users;
        this.userRepository = new UserRepository();
    }


    public Response startBattle(Request request) {
        String token = request.getHeaders().get("Authorization");
        System.out.println("Received token: " + token);

        String username = extractUsername(token);
        System.out.println("Extracted username: " + username);

        User user;
        try {
            user = userRepository.findByUsername(username);
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return new Response(500, "Internal server error");
        }

        if (user == null) {
            System.out.println("User not found: " + username);
            return new Response(401, "Unauthorized");
        }

        List<MonsterCard> deck = user.getDeck();
        System.out.println("Deck size: " + (deck != null ? deck.size() : "null"));
        if (deck == null || deck.size() != 4) {
            return new Response(400, "Invalid deck. Please configure your deck before battling.");
        }


        battleQueue.offer(username);

        if (battleQueue.size() >= 2) {
            String player1 = battleQueue.poll();
            String player2 = battleQueue.poll();

            User user1;
            User user2;
            try {
                user1 = userRepository.findByUsername(player1);
                user2 = userRepository.findByUsername(player2);
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                return new Response(500, "Internal server error");
            }

            Battle battle = new Battle(player1, user1.getDeck(), player2, user2.getDeck());
            String battleLog = battle.executeBattle();

            updateUserStats(user1, user2, battleLog);

            return new Response(200, battleLog);
        } else {
            return new Response(202, "Waiting for opponent");
        }
    }


    private void updateUserStats(User user1, User user2, String battleLog) {
        int k = 32; // K-Faktor für ELO-Berechnung

        if (battleLog.contains("Winner: " + user1.getUsername())) {
            double expectedScoreWinner = 1 / (1 + Math.pow(10, (user2.getElo() - user1.getElo()) / 400.0));
            double expectedScoreLoser = 1 - expectedScoreWinner;

            user1.setElo((int) (user1.getElo() + k * (1 - expectedScoreWinner)));
            user2.setElo((int) (user2.getElo() + k * (0 - expectedScoreLoser)));

            user1.addWin();
            user2.addLoss();
        } else if (battleLog.contains("Winner: " + user2.getUsername())) {
            double expectedScoreWinner = 1 / (1 + Math.pow(10, (user1.getElo() - user2.getElo()) / 400.0));
            double expectedScoreLoser = 1 - expectedScoreWinner;

            user2.setElo((int) (user2.getElo() + k * (1 - expectedScoreWinner)));
            user1.setElo((int) (user1.getElo() + k * (0 - expectedScoreLoser)));

            user2.addWin();
            user1.addLoss();
        } else {
            System.out.println("Battle ended in a draw. No changes to ELO or stats.");
            return;
        }

        try {
            userRepository.update(user1);
            userRepository.update(user2);

            System.out.println("Stats updated successfully for " + user1.getUsername() + " and " + user2.getUsername());
        } catch (SQLException e) {
            System.out.println("Error updating user stats: " + e.getMessage());
            e.printStackTrace();
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
