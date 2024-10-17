package Controller;

import Models.ElementType;
import Models.MonsterCard;
import Models.User;

public class Battle {

    private User player1;
    private User player2;

    public Battle(User player1, User player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    // Starte den Kampf zwischen zwei Spielern
    public void startBattle() {
        Card card1 = player1.getDeck().getRandomCard();
        Card card2 = player2.getDeck().getRandomCard();

        double damage1 = calculateDamage(card1, card2);
        double damage2 = calculateDamage(card2, card1);

        if (damage1 > damage2) {
            System.out.println(player1.getUsername() + " gewinnt die Runde!");
        } else if (damage2 > damage1) {
            System.out.println(player2.getUsername() + " gewinnt die Runde!");
        } else {
            System.out.println("Unentschieden!");
        }
    }

    // Berechnung des Schadens basierend auf den Elementen
    private double calculateDamage(Card attacker, Card defender) {
        double damage = attacker.getDamage();

        // Elementbasierte Schadensmodifikationen
        if (attacker instanceof MonsterCard && defender instanceof MonsterCard) {
            return damage; // Keine Elementwirkung bei Monstern
        }

        // Effektivitätslogik für SpellCards
        if (attacker.getElementType() == ElementType.WATER && defender.getElementType() == ElementType.FIRE) {
            return damage * 2;
        } else if (attacker.getElementType() == ElementType.FIRE && defender.getElementType() == ElementType.NORMAL) {
            return damage * 2;
        } else if (attacker.getElementType() == ElementType.NORMAL && defender.getElementType() == ElementType.WATER) {
            return damage * 2;
        } else if (defender.getElementType() == ElementType.WATER && attacker.getElementType() == ElementType.FIRE) {
            return damage / 2;
        }

        // Spezialregeln
        if (attacker instanceof GoblinCard && defender instanceof DragonCard) {
            return 0; // Goblins greifen keine Drachen an
        }
        if (attacker instanceof WizardCard && defender instanceof OrkCard) {
            return 0; // Orks werden von Zauberern kontrolliert
        }
        if (attacker instanceof WaterSpellCard && defender instanceof KnightCard) {
            return Double.MAX_VALUE; // Ritter ertrinken sofort bei Wassersprüchen
        }
        if (defender instanceof KrakenCard) {
            return 0; // Kraken sind immun gegen Zaubersprüche
        }
        if (attacker instanceof FireElfCard && defender instanceof DragonCard) {
            return 0; // FireElves weichen Drachenangriffen aus
        }

        return damage;
    }

    public abstract static class Card {
        protected String name;
        protected double damage;
        protected ElementType elementType;  // Hinzufügen des Elements

        public Card(String name, double damage, ElementType elementType) {
            this.name = name;
            this.damage = damage;
            this.elementType = elementType;
        }

        public abstract double attack();

        // Getter und Setter für das Element
        public ElementType getElementType() {
            return elementType;
        }

        public void setElementType(ElementType elementType) {
            this.elementType = elementType;
        }

        // Getter und Setter für Name und Schaden
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getDamage() {
            return damage;
        }

        public void setDamage(double damage) {
            this.damage = damage;
        }
    }
}
