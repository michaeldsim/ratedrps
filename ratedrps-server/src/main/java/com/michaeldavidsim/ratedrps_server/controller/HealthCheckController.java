package com.michaeldavidsim.ratedrps_server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HealthCheckController {
    
    @GetMapping("/api/health_check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Server is running");
    }
    
}
