package com.michaeldavidsim.ratedrps_server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@CrossOrigin(origins = {"https://ratedrps.vercel.app", "http://localhost:5173"})
public class HealthCheckController {
    
    @GetMapping("/api/health_check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Server is running");
    }
    
}
