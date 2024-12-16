package main.Models;

public class TradingDeal {
    private String id;
    private String cardToTrade;
    private String type;
    private double minimumDamage;

    public TradingDeal(String id, String cardToTrade, String type, double minimumDamage) {
        this.id = id;
        this.cardToTrade = cardToTrade;
        this.type = type;
        this.minimumDamage = minimumDamage;
    }

    // Read-only Properties
    public String getId() {
        return id;
    }

    public String getCardToTrade() {
        return cardToTrade;
    }

    // Properties mit Getter und Setter
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getMinimumDamage() {
        return minimumDamage;
    }

    public void setMinimumDamage(double minimumDamage) {
        this.minimumDamage = minimumDamage;
    }
}
