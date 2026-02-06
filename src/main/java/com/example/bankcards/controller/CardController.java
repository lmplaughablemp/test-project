package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateDTO;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Карты", description = "API для управления банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Создание новой карты")
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CardCreateDTO cardCreateDTO) {
        CardDTO createdCard = cardService.createCard(cardCreateDTO, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации о карте")
    public ResponseEntity<CardDTO> getCard(
            @PathVariable Long id) {
        CardDTO card = cardService.getCardById(id);
        return ResponseEntity.ok(card);
    }

    @GetMapping
    @Operation(summary = "Получение списка карт пользователя с фильтрацией")
    public ResponseEntity<Page<CardDTO>> getUserCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CardDTO> cards = cardService.getUserCards(pageable, status, search);

        return ResponseEntity.ok(cards);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Получение всех карт")
    public ResponseEntity<Page<CardDTO>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<CardDTO> cards = cardService.getAllCards(pageable, status, search);

        return ResponseEntity.ok(cards);
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Блокировка карты")
    public ResponseEntity<CardDTO> blockCard(@PathVariable Long id) {
        CardDTO blockedCard = cardService.blockCard(id);
        return ResponseEntity.ok(blockedCard);
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Активация карты")
    public ResponseEntity<CardDTO> activateCard(@PathVariable Long id) {
        CardDTO activatedCard = cardService.activateCard(id);
        return ResponseEntity.ok(activatedCard);
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Получение баланса карты")
    public ResponseEntity<BigDecimal> getCardBalance(@PathVariable Long id) {
        BigDecimal balance = cardService.getCardBalance(id);
        return ResponseEntity.ok(balance);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление карты")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    @Operation(summary = "Получение активных карт пользователя")
    public ResponseEntity<?> getActiveCards() {
        return ResponseEntity.ok(cardService.getUserActiveCards());
    }
}