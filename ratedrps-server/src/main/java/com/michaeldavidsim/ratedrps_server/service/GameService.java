package com.michaeldavidsim.ratedrps_server.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michaeldavidsim.ratedrps_server.model.GameSession;

@Service
public class GameService {
    
    private final SupabaseClientService clientService;
    private final UserService userService;
    private final String supabaseMatchesEndpoint;
    private final ObjectMapper mapper;

    public GameService(@Autowired SupabaseClientService clientService,
                        @Autowired UserService userService,
                        @Value("${supabase.matches-endpoint}") String supabaseMatchesEndpoint) {
        this.clientService = clientService;
        this.userService = userService;
        this.supabaseMatchesEndpoint = supabaseMatchesEndpoint;
        this.mapper = new ObjectMapper();
    }

    public void finalizeGame(GameSession session) {
        saveMatchToDatabase(session);
        updatePlayerStats(session);
    }

    private void saveMatchToDatabase(GameSession session) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", session.getGameId());
        payload.put("player1_id", session.getPlayer1Id());
        payload.put("player2_id", session.getPlayer2Id());
        payload.put("player1_move", session.getPlayer1Move());
        payload.put("player2_move", session.getPlayer2Move());
        payload.put("player1_username", session.getPlayer1Username());
        payload.put("player2_username", session.getPlayer2Username());
        payload.put("player1_elo_delta", session.getPlayer1EloDelta());
        payload.put("player2_elo_delta", session.getPlayer2EloDelta());

        String result = session.getResult();
        payload.put("winner_id", "draw".equals(result) ? null : result);
        payload.put("created_at", session.getCreatedAt().toString());

        String jsonPayload;
        try {
            jsonPayload = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize game session", e);
        }

        HttpHeaders headers = clientService.createAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        
        ResponseEntity<String> response = clientService.getRestTemplate().postForEntity(
            supabaseMatchesEndpoint, 
            entity, 
            String.class
        );
        
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to finalize game session: " + response.getStatusCode());
        }
    }

    private void updatePlayerStats(GameSession session) {
        if ("draw".equals(session.getResult())) {
            userService.updateUserStats(session.getPlayer1Id(), 0, 0, 1, session.getPlayer1EloDelta());
            userService.updateUserStats(session.getPlayer2Id(), 0, 0, 1, session.getPlayer2EloDelta());
        } else if (session.getResult().equals(session.getPlayer1Id())) {
            userService.updateUserStats(session.getPlayer1Id(), 1, 0, 0, session.getPlayer1EloDelta());
            userService.updateUserStats(session.getPlayer2Id(), 0, 1, 0, session.getPlayer2EloDelta());
        } else {
            userService.updateUserStats(session.getPlayer1Id(), 0, 1, 0, session.getPlayer1EloDelta());
            userService.updateUserStats(session.getPlayer2Id(), 1, 0, 0, session.getPlayer2EloDelta());
        }
    }
}