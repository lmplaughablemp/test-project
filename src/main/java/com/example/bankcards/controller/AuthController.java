package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthenticationRequestDTO;
import com.example.bankcards.dto.AuthenticationResponseDTO;
import com.example.bankcards.dto.RegisterRequestDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "API для регистрации и авторизации")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService,
                          JwtService jwtService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequestDTO request) {
        UserDTO user = userService.registerUser(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody AuthenticationRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );


        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");


        String email = authentication.getName();


        final String jwtToken = jwtService.generateToken(email, role);


        AuthenticationResponseDTO response = new AuthenticationResponseDTO();
        response.setToken(jwtToken);
        response.setEmail(email);
        response.setRole(role.replace("ROLE_", "")); // Убираем префикс ROLE_

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    @Operation(summary = "Проверка валидности токена")
    public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            try {
                String username = jwtService.extractUsername(jwt);
                if (jwtService.validateToken(jwt, username)) {
                    return ResponseEntity.ok().build();
                }
            } catch (Exception e) {

            }
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/profile")
    @Operation(summary = "Получение информации о текущем пользователе")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserDTO userDTO = userService.getUserByEmail(email);
        return ResponseEntity.ok(userDTO);
    }
}