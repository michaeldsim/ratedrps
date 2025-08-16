package com.michaeldavidsim.ratedrps_server.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SupabaseStorageService {
    
    private final SupabaseClientService clientService;
    private final String storageUrl;

    public SupabaseStorageService(@Autowired SupabaseClientService clientService,
                                  @Value("${supabase.storage-url}") String storageUrl) {
        this.clientService = clientService;
        this.storageUrl = storageUrl;
    }

    public String uploadFile(String bucket, String filePath, MultipartFile file) throws IOException {
        String uploadUrl = storageUrl + "/object/" + bucket + "/" + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + clientService.getServiceRoleKey());
        headers.set("Content-Type", file.getContentType());
        headers.set("Cache-Control", "3600");

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        ResponseEntity<Map> response = clientService.getRestTemplate().exchange(
            uploadUrl, 
            HttpMethod.POST, 
            requestEntity, 
            Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
            return getPublicUrl(bucket, filePath);
        } else {
            throw new RuntimeException("Failed to upload file to Supabase Storage");
        }
    }

    public String getPublicUrl(String bucket, String filePath) {
        return storageUrl + "/object/public/" + bucket + "/" + filePath;
    }

    public byte[] downloadFile(String bucket, String filePath) {
        String downloadUrl = storageUrl + "/object/" + bucket + "/" + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + clientService.getServiceRoleKey());

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = clientService.getRestTemplate().exchange(
            downloadUrl, 
            HttpMethod.GET, 
            requestEntity, 
            byte[].class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to download file from Supabase Storage");
        }
    }

    public boolean deleteFile(String bucket, String filePath) {
        String deleteUrl = storageUrl + "/object/" + bucket + "/" + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + clientService.getServiceRoleKey());

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = clientService.getRestTemplate().exchange(
            deleteUrl, 
            HttpMethod.DELETE, 
            requestEntity, 
            Void.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }
}