public class MonsterCard extends Card {

    public MonsterCard(String name, double damage, ElementType elementType) {
        super(name, damage, elementType);
    }

    @Override
    public double attack() {
        // Angriffsschaden zurückgeben
        return this.damage;
    }
}

