package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ аутентификации")
public class AuthenticationResponseDTO {

    @Schema(description = "JWT токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @Schema(description = "Роль пользователя", example = "ROLE_USER")
    private String role;

    @Schema(description = "Тип токена", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Время жизни токена в секундах", example = "86400")
    private Long expiresIn = 86400L;


    public AuthenticationResponseDTO() {}

    public AuthenticationResponseDTO(String token, String email, String role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}