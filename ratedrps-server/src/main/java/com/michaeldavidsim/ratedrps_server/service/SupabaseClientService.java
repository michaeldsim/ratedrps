package com.michaeldavidsim.ratedrps_server.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SupabaseClientService {
    
    private final RestTemplate restTemplate;
    private final String supabaseUrl;
    private final String serviceRoleKey;

    public SupabaseClientService(RestTemplate restTemplate,
                                @Value("${supabase.url}") String supabaseUrl,
                                 @Value("${supabase.service-role-key}") String serviceRoleKey) {
        this.restTemplate = restTemplate;
        this.supabaseUrl = supabaseUrl;
        this.serviceRoleKey = serviceRoleKey;
    }

    public HttpHeaders createAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apiKey", serviceRoleKey);
        headers.setBearerAuth(serviceRoleKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public String getSupabaseUrl() { return supabaseUrl; }
    public String getServiceRoleKey() { return serviceRoleKey; }
}