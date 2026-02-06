package com.example.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Тестовые эндпоинты", description = "Эндпоинты для тестирования и отладки")
public class TestController {

    @GetMapping("/public")
    @Operation(summary = "Публичный тестовый эндпоинт", description = "Доступен без аутентификации")
    public Map<String, Object> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Это публичный тестовый эндпоинт");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("access", "PUBLIC");
        return response;
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Тестовый эндпоинт для USER", description = "Доступен только пользователям с ролью USER")
    public Map<String, Object> userTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Это тестовый эндпоинт для USER");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("access", "USER_ONLY");
        return response;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Тестовый эндпоинт для ADMIN", description = "Доступен только пользователям с ролью ADMIN")
    public Map<String, Object> adminTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Это тестовый эндпоинт для ADMIN");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("access", "ADMIN_ONLY");
        return response;
    }

    @GetMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Тестовый эндпоинт для аутентифицированных", description = "Доступен всем аутентифицированным пользователям")
    public Map<String, Object> authenticatedTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", " Это тестовый эндпоинт для аутентифицированных пользователей");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("access", "AUTHENTICATED");
        return response;
    }
}