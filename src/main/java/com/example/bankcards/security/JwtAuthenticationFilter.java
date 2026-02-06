package com.example.bankcards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        logger.debug("JWT Filter - Request URI: {}", request.getRequestURI());
        logger.debug("JWT Filter - Authorization header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("JWT Filter - No Bearer token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        logger.debug("JWT Filter - Extracted token: {}...", jwt.substring(0, Math.min(20, jwt.length())));

        try {
            userEmail = jwtService.extractUsername(jwt);
            logger.debug("JWT Filter - Extracted username: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("JWT Filter - Loading user details for: {}", userEmail);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                logger.debug("JWT Filter - User details loaded, authorities: {}", userDetails.getAuthorities());

                if (jwtService.validateToken(jwt, userDetails)) {
                    logger.debug("JWT Filter - Token is valid, setting authentication");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("JWT Filter - Authentication set in SecurityContext");
                } else {
                    logger.warn("JWT Filter - Token validation failed for user: {}", userEmail);
                }
            } else {
                if (userEmail == null) {
                    logger.warn("JWT Filter - Could not extract username from token");
                } else {
                    logger.debug("JWT Filter - Authentication already exists in context");
                }
            }
        } catch (Exception e) {
            logger.error("JWT Filter - Error processing JWT token: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}