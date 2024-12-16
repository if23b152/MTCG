package main.Models;

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

    // Setter
    public void setName(String name) { this.name = name; }
    public void setBio(String bio) { this.bio = bio; }
    public void setImage(String image) { this.image = image; }

    public void updateProfile(String name, String bio, String image) {
        this.name = name;
        this.bio = bio;
        this.image = image;
    }

    public String toJson() {
        return String.format("{\"Name\":\"%s\",\"Bio\":\"%s\",\"Image\":\"%s\"}",
                name != null ? name : "",
                bio != null ? bio : "",
                image != null ? image : "");
    }
    public void addWin() {
        this.wins++;
        this.elo += 3;
    }

    public void addLoss() {
        this.losses++;
        this.elo = Math.max(0, this.elo - 5);
    }

    // Getter für die neuen Felder



}

