package com.michaeldavidsim.ratedrps_server.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michaeldavidsim.ratedrps_server.model.GameSession;
import com.michaeldavidsim.ratedrps_server.model.User;

@Service
public class SupabaseService {

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.matches-endpoint}")
    private String supabaseMatchesEndpoint;

    @Value("${supabase.users-endpoint}")
    private String supabaseUsersEndpoint;

    @Value("${supabase.increment-user-stats-endpoint}")
    private String supabaseIncrementUserStatsEndpoint;

    public void finalizeGame(GameSession session) {
        ObjectMapper mapper = new ObjectMapper();
        
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apiKey", serviceRoleKey);
        headers.setBearerAuth(serviceRoleKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(supabaseMatchesEndpoint, entity, String.class);
        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to finalize game session: " + response.getStatusCode());
        }

        // Update player stats
        if("draw".endsWith(session.getResult())) {
            updateUserStats(session.getPlayer1Id(), 0, 0, 1, session.getPlayer1EloDelta());
            updateUserStats(session.getPlayer2Id(), 0, 0, 1, session.getPlayer2EloDelta());
        } else if(session.getResult().equals(session.getPlayer1Id())) {
            updateUserStats(session.getPlayer1Id(), 1, 0, 0, session.getPlayer1EloDelta());
            updateUserStats(session.getPlayer2Id(), 0, 1, 0, session.getPlayer2EloDelta());
        } else {
            updateUserStats(session.getPlayer1Id(), 0, 1, 0, session.getPlayer1EloDelta());
            updateUserStats(session.getPlayer2Id(), 1, 0, 0, session.getPlayer2EloDelta());
        }
    }

    public User getUserStats(String userId) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("apiKey", serviceRoleKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        String url = String.format("%s?id=eq.%s", supabaseUsersEndpoint, userId);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<User[]> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            entity,
            User[].class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to fetch user stats: " + response.getStatusCode());
        }

        return response.getBody()[0];
    }

    public void updateUserStats(String userId, int winDelta, int lossDelta, int drawDelta, int eloDelta) {
        ObjectMapper mapper = new ObjectMapper();
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("uuid", userId);
        payload.put("win_delta", winDelta);
        payload.put("loss_delta", lossDelta);
        payload.put("draw_delta", drawDelta);
        payload.put("elo_delta", eloDelta);

        String jsonPayload;
        try {
            jsonPayload = mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize user stats", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(serviceRoleKey);
        headers.set("apiKey", serviceRoleKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(supabaseIncrementUserStatsEndpoint, entity, String.class);
        
        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException("Failed to update user stats: " + response.getStatusCode());
        }
    }
}