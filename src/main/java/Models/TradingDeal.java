package Models;

import java.util.UUID;

public class TradingDeal {
    private final UUID id;
    private final UUID cardToTrade;
    private final String type;
    private final double minimumDamage;

    public TradingDeal(UUID id, UUID cardToTrade, String type, double minimumDamage) {
        this.id = id;
        this.cardToTrade = cardToTrade;
        this.type = type;
        this.minimumDamage = minimumDamage;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCardToTrade() {
        return cardToTrade;
    }

    public String getType() {
        return type;
    }

    public double getMinimumDamage() {
        return minimumDamage;
    }
}