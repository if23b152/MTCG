package Models;

import java.util.List;
import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private String name;
    private String bio;
    private String image;
    private int coins = 20;
    private int elo = 100;
    private int wins = 0;
    private int losses = 0;
    private List<MonsterCard> deck;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.deck = new ArrayList<>();
    }

    public User(String username, String password, int coins, String name, String bio, String image, int elo, int wins, int losses) {
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.elo = elo;
        this.wins = wins;
        this.losses = losses;
        this.deck = new ArrayList<>();
    }

    public List<MonsterCard> getDeck() {
        return deck;
    }

    public void setDeck(List<MonsterCard> deck) {
        this.deck = deck;
    }

    // Getter
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getBio() { return bio; }
    public String getImage() { return image; }
    public int getCoins() { return coins; }
    public int getElo() { return elo; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }


    public void setName(String name) { this.name = name; }
    public void setBio(String bio) { this.bio = bio; }
    public void setImage(String image) { this.image = image; }

    public void setElo(int elo) {
        this.elo = Math.max(0, elo);
    }

    public void updateProfile(String name, String bio, String image) {
        if (name != null) {
            this.name = name;
        }
        if (bio != null) {
            this.bio = bio;
        }
        if (image != null) {
            this.image = image;
        }
    }

    public String toJson() {
        return String.format(
                "{\"Username\":\"%s\",\"Name\":\"%s\",\"Bio\":\"%s\",\"Image\":\"%s\",\"Coins\":%d,\"Elo\":%d,\"Wins\":%d,\"Losses\":%d}",
                username != null ? username : "",
                name != null ? name : "",
                bio != null ? bio : "",
                image != null ? image : "",
                coins,
                elo,
                wins,
                losses
        );
    }

    public void addWin() {
        this.wins++;
        this.elo += 3;
    }

    public void addLoss() {
        this.losses++;
        this.elo = Math.max(0, this.elo - 5);
    }
}
