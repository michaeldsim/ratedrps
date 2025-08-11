package com.michaeldavidsim.ratedrps_server.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HealthCheckRateLimiter implements HandlerInterceptor {

    private static final long WINDOW_MS = 10_000; // 10 seconds
    private static final int MAX_REQUESTS = 5;

    private final Map<String, AccessLog> accessMap = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (request.getRequestURI().equals("/api/health_check")) {
            String clientIp = request.getRemoteAddr();
            AccessLog log = accessMap.computeIfAbsent(clientIp, k -> new AccessLog());

            synchronized (log) {
                long now = System.currentTimeMillis();

                // Reset if window expired
                if (now - log.startTime > WINDOW_MS) {
                    log.startTime = now;
                    log.count = 0;
                }

                log.count++;

                if (log.count > MAX_REQUESTS) {
                    response.setStatus(429);
                    response.getWriter().write("Rate limit exceeded");
                    return false;
                }
            }
        }

        return true;
    }

    private static class AccessLog {
        long startTime = System.currentTimeMillis();
        int count = 0;
    }
}
