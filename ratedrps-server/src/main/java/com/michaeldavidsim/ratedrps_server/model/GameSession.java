package com.michaeldavidsim.ratedrps_server.model;

import java.time.Instant;

public class GameSession {

    private String gameId;
    private GamePlayer player1;
    private GamePlayer player2;
    private int player1EloDelta;
    private int player2EloDelta;
    private String result;
    private Instant createdAt;

    public GameSession() {}

    public GameSession(String gameId, GamePlayer player1, GamePlayer player2) {
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.result = null;
        this.createdAt = Instant.now();
    }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public GamePlayer getPlayer1() { return player1; }
    public void setPlayer1(GamePlayer player1) { this.player1 = player1; }

    public GamePlayer getPlayer2() { return player2; }
    public void setPlayer2(GamePlayer player2) { this.player2 = player2; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getPlayer1Username() { return player1 != null ? player1.getUsername() : null; }
    public String getPlayer2Username() { return player2 != null ? player2.getUsername() : null; }
    public String getPlayer1Id() { return player1 != null ? player1.getUserId() : null; }
    public String getPlayer2Id() { return player2 != null ? player2.getUserId() : null; }

    public String getPlayer1Move() { return player1 != null ? player1.getMove() : null; }
    public void setPlayer1Move(String move) { if (player1 != null) player1.setMove(move); }
    public String getPlayer2Move() { return player2 != null ? player2.getMove() : null; }
    public void setPlayer2Move(String move) { if (player2 != null) player2.setMove(move); }

    public int getPlayer1EloDelta() { return player1EloDelta; }
    public void setPlayer1EloDelta(int delta) { this.player1EloDelta = delta; }
    public int getPlayer2EloDelta() { return player2EloDelta; }
    public void setPlayer2EloDelta(int delta) { this.player2EloDelta = delta; }

    @Override
    public String toString() {
        return "GameSession{" +
                "gameId='" + gameId + '\'' +
                ", player1=" + player1 +
                ", player2=" + player2 +
                ", result='" + result + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
