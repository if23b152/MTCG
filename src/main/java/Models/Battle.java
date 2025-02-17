package Models;

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
        double damage1 = calculateEffectiveDamage(card1, card2);
        double damage2 = calculateEffectiveDamage(card2, card1);
        applyBooster(getRandomCard(deck1));
        applyBooster(getRandomCard(deck2));

        if (damage1 > damage2) {
            boolean removed = deck2.remove(card2);
            if (removed) {
                deck1.add(card2);
                logCardMovement("Card moved", card2, player2, player1);
            } else {
                battleLog.append("Failed to remove card ").append(card2.getName())
                        .append(" from ").append(player2).append("'s deck\n");
            }
        } else if (damage2 > damage1) {
            boolean removed = deck1.remove(card1);
            if (removed) {
                deck2.add(card1);
                logCardMovement("Card moved", card1, player1, player2);
            } else {
                battleLog.append("Failed to remove card ").append(card1.getName())
                        .append(" from ").append(player1).append("'s deck\n");
            }
        } else {
            battleLog.append("Round ").append(rounds).append(" ends in a draw\n");
        }

        if (deck1.isEmpty()) {
            battleLog.append("\nGame Over - Winner: ").append(player2).append("\n");
            return;
        }
        if (deck2.isEmpty()) {
            battleLog.append("\nGame Over - Winner: ").append(player1).append("\n");
            return;
        }

    }



    private void logCardMovement(String action, MonsterCard card, String from, String to) {
        System.out.println(action + ": " + card.getName() + " (ID: " + card.getId() + ") moved from " + from + " to " + to);
    }


    public double calculateEffectiveDamage(MonsterCard attacker, MonsterCard defender) {
        if (attacker.getName().equals("Goblin") && defender.getName().equals("Dragon")) {
            battleLog.append("Special case: Goblin is too afraid to attack Dragon.\n");
            return 0;
        }
        if (attacker.getName().equals("Kraken") && defender.getName().contains("Spell")) {
            battleLog.append("Special case: Kraken is immune to spells.\n");
            return Double.MAX_VALUE;
        }
        if (attacker.getName().equals("Knight") && defender.getName().equals("WaterSpell")) {
            battleLog.append("Special case: Knight drowns due to WaterSpell.\n");
            return 0;
        }


        double baseDamage = attacker.getDamage();
        double multiplier = attacker.getElementType().calculateDamageModifier(defender.getElementType());
        return baseDamage * multiplier;
    }




    public MonsterCard getRandomCard(List<MonsterCard> deck) {
        if (deck.isEmpty()) {
            throw new IllegalStateException("Deck is empty, cannot select a random card.");
        }
        int index = (int) (Math.random() * deck.size());
        System.out.println("Selected random card: " + deck.get(index).getName());
        return deck.get(index);
    }


    private void determineWinner() {
        if (deck1.isEmpty()) {
            battleLog.append("\nGame Over - Winner: ").append(player2).append("\n");
            return;
        }
        if (deck2.isEmpty()) {
            battleLog.append("\nGame Over - Winner: ").append(player1).append("\n");
            return;
        }
        if (rounds >= 100) {
            battleLog.append("\nGame Over - Draw after 100 rounds\n"); //draw
        }
    }

    public void applyBooster(MonsterCard card) {
        double originalDamage = card.getDamage();
        card.setDamage(originalDamage * 1.5);  // Booster erhöht den Schaden um 50%
        battleLog.append("Booster applied to ").append(card.getName())
                .append(" (New Damage: ").append(card.getDamage()).append(")\n");
    }

}
