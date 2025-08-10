package com.michaeldavidsim.ratedrps_server.model;

public class GamePlayer {
    private final String userId;
    private final String username;
    private String move;

    public GamePlayer(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getMove() {
        return move;
    }

    public void setMove(String move) {
        this.move = move;
    }

    // easier for comparisons in sets and maps
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GamePlayer)) return false;
        GamePlayer that = (GamePlayer) o;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}
