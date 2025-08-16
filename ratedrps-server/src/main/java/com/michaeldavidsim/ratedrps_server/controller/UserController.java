package com.michaeldavidsim.ratedrps_server.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.michaeldavidsim.ratedrps_server.service.SupabaseStorageService;
import com.michaeldavidsim.ratedrps_server.service.UserService;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private SupabaseStorageService storageService;


    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("avatar") MultipartFile file,
            @RequestParam("userId") String userId) {
        
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("No file uploaded"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("File size must be less than 5MB"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Only image files are allowed"));
            }

            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String fileName = userId + "-" + System.currentTimeMillis() + "." + fileExtension;
            String filePath = userId + "/" + fileName;

            String publicUrl = storageService.uploadFile("avatars", filePath, file);

            AvatarUploadResponse response = new AvatarUploadResponse(
                true, 
                publicUrl, 
                filePath,
                "Avatar uploaded successfully"
            );

            userService.updateUserAvatarUrl(userId, filePath);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to process file upload"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/avatar/{filename}")
    public ResponseEntity<?> getAvatar(@PathVariable String filename) {
        try {
            byte[] avatarData = storageService.downloadFile("avatars", filename);
            
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header("Cache-Control", "public, max-age=3600")
                .body(avatarData);
                
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public static class AvatarUploadResponse {
        private boolean success;
        private String avatarUrl;
        private String avatarPath;
        private String message;

        public AvatarUploadResponse(boolean success, String avatarUrl, String avatarPath, String message) {
            this.success = success;
            this.avatarUrl = avatarUrl;
            this.avatarPath = avatarPath;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getAvatarPath() { return avatarPath; }
        public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ErrorResponse {
        private String error;
        private boolean success = false;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
    }
    
}
