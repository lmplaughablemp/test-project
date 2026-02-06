package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            throw new UsernameNotFoundException("Пользователь не аутентифицирован");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден с email: " + email));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean isAdmin() {
        User user = getCurrentUser();
        return user.getRoles().stream()
                .anyMatch(role -> role.name().equals("ROLE_ADMIN"));
    }

    public boolean isCurrentUser(Long userId) {
        return getCurrentUserId().equals(userId);
    }
}