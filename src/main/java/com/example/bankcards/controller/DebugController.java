package com.example.bankcards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/debug")
@Tag(name = "Отладка", description = "API для отладки")
public class DebugController {

    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);

    @GetMapping("/auth")
    @Operation(summary = "Отладка аутентификации")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        logger.info("=== DEBUG AUTHENTICATION ===");
        logger.info("Authentication: {}", auth);
        logger.info("Principal: {}", auth.getPrincipal());
        logger.info("Authorities: {}", auth.getAuthorities());
        logger.info("Is authenticated: {}", auth.isAuthenticated());
        logger.info("Name: {}", auth.getName());
        logger.info("Details: {}", auth.getDetails());

        String result = String.format(
                "Auth: %s\nPrincipal: %s\nAuthorities: %s\nAuthenticated: %s\nName: %s",
                auth, auth.getPrincipal(), auth.getAuthorities(), auth.isAuthenticated(), auth.getName()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/headers")
    @Operation(summary = "Показать заголовки запроса")
    public ResponseEntity<String> debugHeaders(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("=== DEBUG HEADERS ===");
        logger.info("Authorization Header: {}", authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            logger.info("Token length: {}", token.length());
            logger.info("Token preview: {}...", token.substring(0, Math.min(20, token.length())));
        }

        return ResponseEntity.ok("Headers logged, check console");
    }
}