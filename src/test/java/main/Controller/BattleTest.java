package main.Controller;

import Models.Battle;
import Models.MonsterCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class BattleTest {
    @Test
    void executeBattle_shouldDetermineWinner() {
        List<MonsterCard> deck1 = new ArrayList<>(List.of(
                new MonsterCard(UUID.fromString("e85e3976-7c86-4d06-9a80-641c2019a79f"), "Watergoblin", 10.0),
                new MonsterCard(UUID.fromString("99f8f8dc-e25e-4a95-aa2c-782823f36e2a"), "Firegoblin", 20.0)
        ));

        List<MonsterCard> deck2 = new ArrayList<>(List.of(
                new MonsterCard(UUID.fromString("1b6bab86-bdb2-47e5-b6e4-68c5ab389334"), "Earthgoblin", 15.0),
                new MonsterCard(UUID.fromString("d84e4eec-5d7b-4e24-ae32-b3c17bf95b51"), "Dragon", 50.0)
        ));

        Battle battle = new Battle("Player1", deck1, "Player2", deck2);

        String result = battle.executeBattle();

        assertTrue(result.contains("Game Over - Winner:"), "The battle should determine a winner or end in a draw.");
    }
    @Test
    void calculateEffectiveDamage_shouldHandleSpecialCaseGoblinVsDragon() {
        MonsterCard goblin = new MonsterCard(UUID.randomUUID(), "Goblin", 10);
        MonsterCard dragon = new MonsterCard(UUID.randomUUID(), "Dragon", 50);
        Battle battle = new Battle("Player1", new ArrayList<>(List.of(goblin)), "Player2", new ArrayList<>(List.of(dragon)));

        double damage = battle.calculateEffectiveDamage(goblin, dragon);

        assertEquals(0, damage, "Goblin should deal no damage to Dragon.");
    }

    @Test
    void getRandomCard_shouldThrowExceptionForEmptyDeck() {
        List<MonsterCard> emptyDeck = List.of();
        Battle battle = new Battle("Player1", emptyDeck, "Player2", List.of());

        assertThrows(IllegalStateException.class, () -> battle.getRandomCard(emptyDeck), "Should throw exception for empty deck.");
    }
    @Test
    void applyBooster_shouldIncreaseCardDamage() {
        MonsterCard card = new MonsterCard(UUID.randomUUID(), "Elf", 20);
        double originalDamage = card.getDamage();
        Battle battle = new Battle("Player1", new ArrayList<>(List.of(card)), "Player2", new ArrayList<>());

        battle.applyBooster(card);

        assertEquals(originalDamage * 1.5, card.getDamage(), 0.001, "Booster should increase damage by 50%");
    }

}
