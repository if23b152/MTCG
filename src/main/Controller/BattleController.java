package main.Controller;

import main.Models.MonsterCard;
import main.Models.User;
import main.Models.Battle;
import main.Server.Request;
import main.Server.Response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BattleController {
    private final Map<String, User> users;
    private final ConcurrentLinkedQueue<String> battleQueue = new ConcurrentLinkedQueue<>();

    public BattleController(Map<String, User> users) {
        this.users = users;
    }

    public Response startBattle(Request request) {
        String token = request.getHeaders().get("Authorization");
        String username = extractUsername(token);

        User user = users.get(username);
        if (user == null) {
            return new Response(401, "Unauthorized");
        }

        List<MonsterCard> deck = user.getDeck();
        if (deck == null || deck.size() != 4) {
            return new Response(400, "Invalid deck. Please configure your deck before battling.");
        }

        battleQueue.offer(username);

        if (battleQueue.size() >= 2) {
            String player1 = battleQueue.poll();
            String player2 = battleQueue.poll();

            User user1 = users.get(player1);
            User user2 = users.get(player2);

            Battle battle = new Battle(player1, user1.getDeck(), player2, user2.getDeck());
            String battleLog = battle.executeBattle();

            updateUserStats(user1, user2, battleLog);

            return new Response(200, battleLog);
        } else {
            return new Response(202, "Waiting for opponent");
        }
    }

    private void updateUserStats(User user1, User user2, String battleLog) {
        if (battleLog.contains("Winner: " + user1.getUsername())) {
            user1.addWin();
            user2.addLoss();
        } else if (battleLog.contains("Winner: " + user2.getUsername())) {
            user2.addWin();
            user1.addLoss();
        }
        // In case of a draw, no stats are updated
    }

    private String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7, token.length() - 9);
        }
        return "";
    }
}
