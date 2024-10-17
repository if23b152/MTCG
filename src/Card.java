public abstract class Card {
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
