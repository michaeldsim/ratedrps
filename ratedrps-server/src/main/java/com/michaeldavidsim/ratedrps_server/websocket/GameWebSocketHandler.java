package com.michaeldavidsim.ratedrps_server.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michaeldavidsim.ratedrps_server.model.GamePlayer;
import com.michaeldavidsim.ratedrps_server.model.GameSession;
import com.michaeldavidsim.ratedrps_server.model.User;
import com.michaeldavidsim.ratedrps_server.service.EloService;
import com.michaeldavidsim.ratedrps_server.service.SupabaseService;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();

    // Thread-safe collections
    private final Map<String, WebSocketSession> lobbyPlayers = new ConcurrentHashMap<>();
    private final Queue<GamePlayer> matchmakingQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, String> userGameMap = new ConcurrentHashMap<>();
    private final Map<String, Set<WebSocketSession>> gameRooms = new ConcurrentHashMap<>();
    private final Map<String, GameSession> gameMap = new ConcurrentHashMap<>();

    private final SupabaseService supabaseService;
    private final EloService eloService;

    public GameWebSocketHandler(SupabaseService supabaseService, EloService eloService) {
        this.supabaseService = supabaseService;
        this.eloService = eloService;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: {}", session.getId());
        cleanupUserSession(session);
        super.afterConnectionClosed(session, status);
    }

    private void cleanupUserSession(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) return;

        // Remove from lobby and matchmaking queue
        lobbyPlayers.remove(userId);
        matchmakingQueue.remove(new GamePlayer(userId, "dummy")); // dummy username for removal

        // Remove from game if playing
        String gameId = userGameMap.remove(userId);
        if (gameId != null) {
            Set<WebSocketSession> sessions = gameRooms.get(gameId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    gameRooms.remove(gameId);
                    gameMap.remove(gameId);
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Received message: {}", message.getPayload());
        Map<String, Object> payload = mapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        switch (type) {
            case "JOIN_LOBBY" -> handleJoinLobby(session, payload);
            case "LEAVE_LOBBY" -> handleLeaveLobby(session, payload);
            case "MAKE_MOVE" -> handleMakeMove(session, payload);
            default -> sendError(session, "Unknown message type: " + type);
        }
    }

    private void handleJoinLobby(WebSocketSession session, Map<String, Object> payload) throws Exception {
        String userId = (String) payload.get("userId");
        String username = (String) payload.get("username");
        if (userId == null || userId.isBlank() || username == null || username.isBlank()) {
            sendError(session, "Missing or invalid userId or username");
            return;
        }

        session.getAttributes().put("userId", userId);
        lobbyPlayers.put(userId, session);

        GamePlayer player = new GamePlayer(userId, username);

        if (!matchmakingQueue.contains(player)) {
            matchmakingQueue.add(player);
        }

        broadcastLobbyUpdate();
        tryMatchmake();
    }


    private void handleLeaveLobby(WebSocketSession session, Map<String, Object> payload) throws Exception {
        String userId = (String) payload.get("userId");
        if (userId != null) {
            // create a dummy GamePlayer for removal since equals() is based on userId
            matchmakingQueue.remove(new GamePlayer(userId, "dummy"));
            lobbyPlayers.remove(userId);
            broadcastLobbyUpdate();
        }
    }


    private void tryMatchmake() throws Exception {
        while (matchmakingQueue.size() >= 2) {
            GamePlayer player1 = matchmakingQueue.poll();
            GamePlayer player2 = matchmakingQueue.poll();

            if (player1 == null || player2 == null) break;

            WebSocketSession session1 = lobbyPlayers.get(player1.getUserId());
            WebSocketSession session2 = lobbyPlayers.get(player2.getUserId());

            if (session1 == null || session2 == null) {
                if (session1 != null) matchmakingQueue.add(player1);
                if (session2 != null) matchmakingQueue.add(player2);
                continue;
            }

            String gameId = UUID.randomUUID().toString();
            userGameMap.put(player1.getUserId(), gameId);
            userGameMap.put(player2.getUserId(), gameId);

            Set<WebSocketSession> gameRoom = ConcurrentHashMap.newKeySet();
            gameRoom.add(session1);
            gameRoom.add(session2);
            gameRooms.put(gameId, gameRoom);

            GameSession gameSession = new GameSession(
                gameId,
                player1,
                player2
            );
            gameMap.put(gameId, gameSession);

            sendMatchFound(session1, gameId, player2);
            sendMatchFound(session2, gameId, player1);

            lobbyPlayers.remove(player1.getUserId());
            lobbyPlayers.remove(player2.getUserId());

            broadcastLobbyUpdate();
        }
    }


    private void sendMatchFound(WebSocketSession session, String gameId, GamePlayer opponent) throws Exception {
        Map<String, Object> matchFoundPayload = Map.of(
            "type", "MATCH_FOUND",
            "data", Map.of(
                "gameId", gameId,
                "opponentId", opponent.getUserId(),
                "opponentUsername", opponent.getUsername()
            )
        );
        session.sendMessage(new TextMessage(mapper.writeValueAsString(matchFoundPayload)));
    }

    private void handleMakeMove(WebSocketSession session, Map<String, Object> payload) throws Exception {
        String userId = (String) payload.get("userId");
        String gameId = (String) payload.get("gameId");
        String move = (String) payload.get("move");

        if (userId == null || gameId == null || move == null) {
            sendError(session, "Invalid MAKE_MOVE payload");
            return;
        }

        GameSession gameSession = gameMap.get(gameId);
        if (gameSession == null) {
            sendError(session, "Game not found");
            return;
        }

        if (userId.equals(gameSession.getPlayer1Id())) {
            gameSession.getPlayer1().setMove(move);
        } else if (userId.equals(gameSession.getPlayer2Id())) {
            gameSession.getPlayer2().setMove(move);
        } else {
            sendError(session, "User not part of this game");
            return;
        }

        boolean bothMoved = gameSession.getPlayer1Move() != null && gameSession.getPlayer2Move() != null;
        if (bothMoved) {
            String winner = determineWinner(gameSession.getPlayer1Move(), gameSession.getPlayer2Move());
            gameSession.setResult("draw".equals(winner) ? "draw" : winner.equals("player1") ? gameSession.getPlayer1Id() : gameSession.getPlayer2Id());

            User player1Stats = supabaseService.getUserStats(gameSession.getPlayer1Id());
            User player2Stats = supabaseService.getUserStats(gameSession.getPlayer2Id());

            int[] newRatings = eloService.calculateNewRatings(player1Stats.getElo(), player2Stats.getElo(), gameSession.getResult().equals(gameSession.getPlayer1Id()) ? 1.0 : gameSession.getResult().equals(gameSession.getPlayer2Id()) ? 0.0 : 0.5);
    
            int player1EloDelta = newRatings[0] - player1Stats.getElo();
            int player2EloDelta = newRatings[1] - player2Stats.getElo();

            gameSession.setPlayer1EloDelta(player1EloDelta);
            gameSession.setPlayer2EloDelta(player2EloDelta);

            broadcastGameUpdate(gameId, gameSession, true);
            cleanupGame(gameId, gameSession);
        } else {
            broadcastGameUpdate(gameId, gameSession, false);
        }
    }

    private void cleanupGame(String gameId, GameSession gameSession) {
        gameMap.remove(gameId);
        gameRooms.remove(gameId);

        userGameMap.remove(gameSession.getPlayer1Id());
        userGameMap.remove(gameSession.getPlayer2Id());

        try {
            supabaseService.finalizeGame(gameSession);
        } catch (Exception e) {
            logger.error("Error finalizing game in Supabase", e);
        }
    }

    private void sendError(WebSocketSession session, String errorMsg) throws Exception {
        Map<String, Object> errorPayload = Map.of(
            "type", "ERROR",
            "message", errorMsg
        );
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(errorPayload)));
        }
    }

    private void broadcastLobbyUpdate() throws Exception {
        Map<String, Object> lobbyPayload = Map.of(
            "type", "LOBBY_UPDATE",
            "playersWaiting", matchmakingQueue.size()
        );

        String msg = mapper.writeValueAsString(lobbyPayload);
        for (WebSocketSession session : lobbyPlayers.values()) {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(msg));
            }
        }
    }

    private String determineWinner(String move1, String move2) {
        if (move1.equals(move2)) return "draw";

        return switch (move1) {
            case "rock" -> move2.equals("scissors") ? "player1" : "player2";
            case "paper" -> move2.equals("rock") ? "player1" : "player2";
            case "scissors" -> move2.equals("paper") ? "player1" : "player2";
            default -> "draw"; // fallback
        };
    }

    private void broadcastGameUpdate(String gameId, GameSession gameSession, boolean isFinal) throws Exception {
        Set<WebSocketSession> sessions = gameRooms.get(gameId);
        if (sessions == null || sessions.isEmpty()) return;

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("player1Id", gameSession.getPlayer1Id());
        gameData.put("player2Id", gameSession.getPlayer2Id());
        if (gameSession.getPlayer1Move() != null) gameData.put("player1Move", gameSession.getPlayer1Move());
        if (gameSession.getPlayer2Move() != null) gameData.put("player2Move", gameSession.getPlayer2Move());
        if (gameSession.getResult() != null) gameData.put("result", gameSession.getResult());
        if (isFinal) {
            gameData.put("player1EloDelta", gameSession.getPlayer1EloDelta());
            gameData.put("player2EloDelta", gameSession.getPlayer2EloDelta());
        }

        Map<String, Object> payload = Map.of(
            "type", "GAME_UPDATE",
            "data", gameData,
            "isFinal", isFinal
        );

        String msg = mapper.writeValueAsString(payload);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(msg));
            }
        }
    }
}
