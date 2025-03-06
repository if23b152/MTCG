package Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;

public class MonsterCard {
    @JsonProperty("Id")
    private UUID id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Damage")
    private double damage;

    private ElementType elementType;

    public MonsterCard() {}

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

    public String getType() {
        if (name.toLowerCase().contains("spell")) {
            return "spell";
        }
        return "monster";
    }

    // Setter-Methode für den Schaden
    public void setDamage(double damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative.");
        }
        this.damage = damage;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MonsterCard that = (MonsterCard) obj;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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