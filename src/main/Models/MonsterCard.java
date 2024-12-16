package main.Models;

import java.util.UUID;

public class MonsterCard {
    private UUID id;
    private String name;
    private double damage;
    private ElementType elementType;

    public MonsterCard(UUID id, String name, double damage) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.elementType = ElementType.fromCardName(name);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDamage() {
        return damage;
    }

    public ElementType getElementType() {
        return elementType;
    }

    @Override
    public String toString() {
        return "MonsterCard{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                ", elementType=" + elementType +
                '}';
    }
}
