package main.Models;

import main.Models.MonsterCard;
import java.util.List;

public class Battle {
    private final String player1;
    private final String player2;
    private final List<MonsterCard> deck1;
    private final List<MonsterCard> deck2;
    private final StringBuilder battleLog;
    private int rounds;

    public Battle(String player1, List<MonsterCard> deck1, String player2, List<MonsterCard> deck2) {
        this.player1 = player1;
        this.player2 = player2;
        this.deck1 = deck1;
        this.deck2 = deck2;
        this.battleLog = new StringBuilder();
        this.rounds = 0;
    }

    public String executeBattle() {
        battleLog.append("Battle: ").append(player1).append(" vs ").append(player2).append("\n");

        while (rounds < 100 && !deck1.isEmpty() && !deck2.isEmpty()) {
            rounds++;
            battleLog.append("\nRound ").append(rounds).append(":\n");

            MonsterCard card1 = getRandomCard(deck1);
            MonsterCard card2 = getRandomCard(deck2);

            executeRound(card1, card2);
        }

        determineWinner();
        return battleLog.toString();
    }

    private void executeRound(MonsterCard card1, MonsterCard card2) {
        battleLog.append(player1).append(": ").append(card1.getName())
                .append(" (").append(card1.getDamage()).append(" Damage) vs ");
        battleLog.append(player2).append(": ").append(card2.getName())
                .append(" (").append(card2.getDamage()).append(" Damage)\n");

        double damage1 = calculateEffectiveDamage(card1, card2);
        double damage2 = calculateEffectiveDamage(card2, card1);

        if (damage1 > damage2) {
            battleLog.append(player1).append(" wins round ").append(rounds).append("\n");
            deck1.add(card2);
            deck2.remove(card2);
        } else if (damage2 > damage1) {
            battleLog.append(player2).append(" wins round ").append(rounds).append("\n");
            deck2.add(card1);
            deck1.remove(card1);
        } else {
            battleLog.append("Round ").append(rounds).append(" ends in a draw\n");
        }
    }

    private double calculateEffectiveDamage(MonsterCard attacker, MonsterCard defender) {
        double baseDamage = attacker.getDamage();
        double elementMultiplier = attacker.getElementType()
                .calculateDamageModifier(defender.getElementType());
        return baseDamage * elementMultiplier;
    }

    private MonsterCard getRandomCard(List<MonsterCard> deck) {
        int index = (int) (Math.random() * deck.size());
        return deck.get(index);
    }

    private void determineWinner() {
        if (deck1.isEmpty()) {
            battleLog.append("\nGame Over - Winner: ").append(player2);
        } else if (deck2.isEmpty()) {
            battleLog.append("\nGame Over - Winner: ").append(player1);
        } else {
            battleLog.append("\nGame Over - Draw after ").append(rounds).append(" rounds");
        }
    }
}
