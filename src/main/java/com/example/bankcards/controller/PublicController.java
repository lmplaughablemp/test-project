package com.example.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Публичные endpoint'ы", description = "Доступные без аутентификации")
public class PublicController {

    @GetMapping("/health")
    @Operation(summary = "Проверка работоспособности сервиса")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Bank Cards API is running");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Информация о сервисе")
    public ResponseEntity<Map<String, String>> getInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("name", "Bank Cards Management System");
        response.put("version", "1.0.0");
        response.put("description", "API для управления банковскими картами");
        response.put("status", "active");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    @Operation(summary = "Простой пинг")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}