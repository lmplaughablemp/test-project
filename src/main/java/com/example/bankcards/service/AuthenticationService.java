package com.example.bankcards.service;

import com.example.bankcards.dto.AuthenticationRequestDTO;
import com.example.bankcards.dto.AuthenticationResponseDTO;
import com.example.bankcards.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 UserDetailsService userDetailsService,
                                 JwtService jwtService,
                                 UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        final String jwtToken = jwtService.generateToken(request.getEmail(), role);

        userService.updateLastLogin(request.getEmail());

        AuthenticationResponseDTO response = new AuthenticationResponseDTO();
        response.setToken(jwtToken);
        response.setEmail(request.getEmail());
        response.setRole(role.replace("ROLE_", "")); // Убираем префикс ROLE_

        return response;
    }

    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            return jwtService.validateToken(token, username);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsernameFromToken(String token) {
        return jwtService.extractUsername(token);
    }

    public String extractRoleFromToken(String token) {
        return jwtService.extractRole(token);
    }
}