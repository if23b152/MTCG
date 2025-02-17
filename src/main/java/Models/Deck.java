package Models;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    private final List<MonsterCard> cards;

    public Deck() {
        this.cards = new ArrayList<>();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
