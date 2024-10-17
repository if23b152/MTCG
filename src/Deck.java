import java.util.ArrayList;
import java.util.List;

public class Deck {
    private List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        if (cards.size() < 4) {
            cards.add(card);
        } else {
            System.out.println("Deck ist voll.");
        }
    }

    public void removeCard(Card card) {
        cards.remove(card);
    }

    public List<Card> getBestCards() {
        // Logik, um die besten Karten zu erhalten
        return cards.subList(0, Math.min(cards.size(), 4));
    }

    // Getter und Setter Methoden
}
