package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Переводы", description = "API для управления переводами между картами")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Создание перевода между своими картами")
    public ResponseEntity<Transfer> createTransfer(@Valid @RequestBody TransferRequestDTO requestDTO) {
        Transfer transfer = transferService.createTransfer(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(transfer);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение информации о переводе")
    public ResponseEntity<Transfer> getTransfer(@PathVariable Long id) {
        Transfer transfer = transferService.getTransferById(id);
        return ResponseEntity.ok(transfer);
    }

    @GetMapping("/card/{cardId}")
    @Operation(summary = "Получение истории переводов по карте")
    public ResponseEntity<Page<Transfer>> getCardTransfers(
            @PathVariable Long cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Transfer> transfers = transferService.getTransfersByCard(cardId, pageable);

        return ResponseEntity.ok(transfers);
    }

    @GetMapping
    @Operation(summary = "Получение истории переводов пользователя")
    public ResponseEntity<Page<Transfer>> getUserTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Transfer> transfers = transferService.getUserTransfers(pageable);

        return ResponseEntity.ok(transfers);
    }

    @GetMapping("/card/{cardId}/total-sent")
    @Operation(summary = "Получение общей суммы отправленных переводов с карты")
    public ResponseEntity<BigDecimal> getTotalSentAmount(@PathVariable Long cardId) {
        BigDecimal total = transferService.getTotalTransferredAmount(cardId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/card/{cardId}/total-received")
    @Operation(summary = "Получение общей суммы полученных переводов на карту")
    public ResponseEntity<BigDecimal> getTotalReceivedAmount(@PathVariable Long cardId) {
        BigDecimal total = transferService.getTotalReceivedAmount(cardId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/recent")
    @Operation(summary = "Получение последних переводов пользователя")
    public ResponseEntity<List<Transfer>> getRecentTransfers(
            @RequestParam(defaultValue = "5") int limit) {

        List<Transfer> transfers = transferService.getRecentTransfers(limit);
        return ResponseEntity.ok(transfers);
    }
}