package main.Models;

public enum ElementType {
    WATER,
    FIRE,
    NORMAL;

    public static ElementType fromCardName(String cardName) {
        if (cardName.startsWith("Water")) return WATER;
        if (cardName.startsWith("Fire")) return FIRE;
        return NORMAL;
    }

    public double calculateDamageModifier(ElementType opponentType) {
        if (this == WATER && opponentType == FIRE) return 2.0;
        if (this == FIRE && opponentType == NORMAL) return 2.0;
        if (this == NORMAL && opponentType == WATER) return 2.0;
        if (opponentType == this) return 1.0;
        return 0.5;
    }
}
