package com.competition.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal DB connectivity health check; no business logic or table access.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> dbHealth() {
        long start = System.nanoTime();
        Map<String, Object> body = new HashMap<>();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            body.put("ok", true);
            body.put("detail", "db ok");
            body.put("elapsedMs", elapsedMs);
            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            body.put("ok", false);
            body.put("detail", ex.getMessage());
            body.put("elapsedMs", elapsedMs);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
    }
}
