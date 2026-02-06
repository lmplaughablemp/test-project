package com.example.bankcards.service;

import com.example.bankcards.dto.RegisterRequestDTO;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    public UserDTO registerUser(RegisterRequestDTO request) {
        // Проверяем, существует ли пользователь с таким email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Пользователь с email " + request.getEmail() + " уже существует");
        }

        // Создаем нового пользователя
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()));
        user.addRole(Role.ROLE_USER); // Добавляем роль USER

        // Сохраняем пользователя
        User savedUser = userRepository.save(user);

        // Конвертируем в DTO
        return convertToDTO(savedUser);
    }

    public UserDTO getCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        return convertToDTO(currentUser);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с email: " + email));
        return convertToDTO(user);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + id));
        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + id));

        // Обновляем поля
        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                throw new RuntimeException("Пользователь с email " + userDTO.getEmail() + " уже существует");
            }
            user.setEmail(userDTO.getEmail());
        }

        // Обновляем роль, если указана
        if (userDTO.getRole() != null) {
            try {
                Role newRole = Role.valueOf("ROLE_" + userDTO.getRole().toUpperCase());
                user.setRoles(new HashSet<>(Set.of(newRole)));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Неверная роль: " + userDTO.getRole());
            }
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден с id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void addAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + userId));

        user.addRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    public void removeAdminRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + userId));

        user.getRoles().remove(Role.ROLE_ADMIN);
        // Убедимся, что у пользователя осталась хотя бы роль USER
        if (!user.getRoles().contains(Role.ROLE_USER)) {
            user.addRole(Role.ROLE_USER);
        }
        userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с email: " + email));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void updateUserEnabledStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + userId));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден с id: " + userId));

        // Проверяем, что текущий пользователь меняет свой пароль или это делает администратор
        Long currentUserId = securityUtils.getCurrentUserId();
        boolean isAdmin = securityUtils.isAdmin();

        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new RuntimeException("Вы можете изменить только свой пароль");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public long countUsers() {
        return userRepository.count();
    }

    public long countUsersByRole(Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(role))
                .count();
    }

    public List<UserDTO> searchUsers(String searchTerm) {
        return userRepository.findAll().stream()
                .filter(user -> user.getEmail().toLowerCase().contains(searchTerm.toLowerCase()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());

        // Получаем первую роль (обычно у пользователя одна роль)
        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.name().replace("ROLE_", ""))
                .orElse("USER");
        dto.setRole(role);

        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setEnabled(user.isEnabled());
        return dto;
    }

    // Вспомогательный метод для получения User по email (для Spring Security)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}