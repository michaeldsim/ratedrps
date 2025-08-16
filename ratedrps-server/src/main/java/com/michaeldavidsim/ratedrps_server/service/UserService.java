package com.michaeldavidsim.ratedrps_server.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michaeldavidsim.ratedrps_server.model.User;

@Service
public class UserService {
    
    private final SupabaseClientService clientService;
    private final String supabaseUsersEndpoint;
    private final String supabaseIncrementUserStatsEndpoint;
    private final ObjectMapper mapper;

    public UserService(@Autowired SupabaseClientService clientService,
                       @Value("${supabase.users-endpoint}") String supabaseUsersEndpoint,
                       @Value("${supabase.increment-user-stats-endpoint}") String supabaseIncrementUserStatsEndpoint) {
        this.clientService = clientService;
        this.supabaseUsersEndpoint = supabaseUsersEndpoint;
        this.supabaseIncrementUserStatsEndpoint = supabaseIncrementUserStatsEndpoint;
        this.mapper = new ObjectMapper();
    }


    public User getUserStats(String userId) {
        HttpHeaders headers = clientService.createAuthenticatedHeaders();
        headers.setContentType(null);
        
        String url = String.format("%s?id=eq.%s", supabaseUsersEndpoint, userId);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<User[]> response = clientService.getRestTemplate().exchange(
            url,
            HttpMethod.GET,
            entity,
            User[].class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null || response.getBody().length == 0) {
            throw new RuntimeException("Failed to fetch user stats or user not found: " + response.getStatusCode());
        }

        return response.getBody()[0];
    }

    public void updateUserStats(String userId, int winDelta, int lossDelta, int drawDelta, int eloDelta) {
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

        HttpHeaders headers = clientService.createAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        
        ResponseEntity<String> response = clientService.getRestTemplate().postForEntity(
            supabaseIncrementUserStatsEndpoint, 
            entity, 
            String.class
        );
        
        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException("Failed to update user stats: " + response.getStatusCode());
        }
    }

    public String updateUserAvatarUrl(String userId, String publicUrl) throws IOException {
        HttpHeaders headers = clientService.createAuthenticatedHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = String.format("%s?id=eq.%s", supabaseUsersEndpoint, userId);
        Map<String, String> payload = new HashMap<>();
        payload.put("avatar_url", publicUrl);
        String jsonPayload = mapper.writeValueAsString(payload);
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
        ResponseEntity<String> response = clientService.getRestTemplate().exchange(
            url, 
            HttpMethod.PATCH, 
            entity, 
            String.class
        );

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException("Failed to update user avatar URL: " + response.getStatusCode());
        }
        
        return publicUrl;
    }
}
