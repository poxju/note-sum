package com.poxju.proksi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoints for monitoring and keeping the application awake.
 * These endpoints are lightweight and respond quickly to enable fast wake-up
 * from idle state on platforms like Railway or Heroku.
 */
@RestController
public class HealthController {

    @Autowired(required = false)
    private DataSource dataSource;

    /**
     * Ultra-lightweight ping endpoint for quick wake-up checks.
     * This endpoint does minimal work and responds immediately.
     * 
     * @return Simple OK response with timestamp
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("timestamp", Instant.now().toString());
        response.put("service", "proksi");
        return ResponseEntity.ok(response);
    }

    /**
     * Comprehensive health check endpoint that verifies database connectivity.
     * This endpoint performs a quick database check to ensure the application
     * is fully operational.
     * 
     * @return Health status with database connectivity check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        
        // Quick database connectivity check
        boolean dbHealthy = checkDatabase();
        health.put("database", dbHealthy ? "UP" : "DOWN");
        
        if (!dbHealthy) {
            health.put("status", "DOWN");
            return ResponseEntity.status(503).body(health);
        }
        
        return ResponseEntity.ok(health);
    }

    /**
     * Performs a quick database connectivity check.
     * 
     * @return true if database is accessible, false otherwise
     */
    private boolean checkDatabase() {
        if (dataSource == null) {
            return false;
        }
        
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2); // 2 second timeout
        } catch (Exception e) {
            return false;
        }
    }
}
