public class User {
    private String username;
    private String password;
    private int coins = 20;
    private int elo = 100;
    private Deck deck = new Deck();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void register() {
        // Registrierungscode
    }

    public void login() {
        // Logincode
    }

    public void buyPackage() {
        if (coins >= 5) {
            // Kauflogik
            coins -= 5;
        } else {
            System.out.println("Nicht genügend Münzen.");
        }
    }

    public void defineDeck(Deck deck) {
        this.deck = deck;
    }


}

}
