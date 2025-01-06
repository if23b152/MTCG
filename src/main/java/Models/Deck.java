package Models;

import java.util.ArrayList;
import java.util.List;

public class Deck {
    private final List<MonsterCard> cards;
    private static final int DECK_SIZE = 4;

    public Deck() {
        this.cards = new ArrayList<>();
    }



    public List<MonsterCard> getCards() {
        return cards;
    }

    public void clear() {
        cards.clear();
    }

    public boolean isComplete() {
        return cards.size() == DECK_SIZE;
    }

    public void removeCard(MonsterCard card) {
        cards.remove(card);
    }

    public void addWonCard(MonsterCard card) {
        cards.add(card);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
