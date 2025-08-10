package com.michaeldavidsim.ratedrps_server.model;

public class User {
    private String id;
    private int elo;
    private int wins;
    private int losses;
    private int draws;
    private String createdAt;
    private String username;

    public User() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", elo=" + elo +
                ", wins=" + wins +
                ", losses=" + losses +
                ", draws=" + draws +
                ", createdAt=" + createdAt +
                ", username='" + username + '\'' +
                '}';
    }
}