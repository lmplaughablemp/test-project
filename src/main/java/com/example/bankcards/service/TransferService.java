package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequestDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.exception.UnauthorizedCardAccessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final SecurityUtils securityUtils;

    public TransferService(TransferRepository transferRepository,
                           CardRepository cardRepository,
                           SecurityUtils securityUtils) {
        this.transferRepository = transferRepository;
        this.cardRepository = cardRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public Transfer createTransfer(TransferRequestDTO requestDTO) {
        Long currentUserId = securityUtils.getCurrentUserId();

        Card fromCard = cardRepository.findById(requestDTO.getFromCardId())
                .orElseThrow(() -> new CardNotFoundException("Исходная карта не найдена"));

        Card toCard = cardRepository.findById(requestDTO.getToCardId())
                .orElseThrow(() -> new CardNotFoundException("Целевая карта не найдена"));

        if (!fromCard.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedCardAccessException("Исходная карта принадлежит другому пользователю");
        }

        if (!toCard.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedCardAccessException("Целевая карта принадлежит другому пользователю");
        }

        if (fromCard.getId().equals(toCard.getId())) {
            throw new IllegalArgumentException("Нельзя перевести средства на ту же карту");
        }

        if (!fromCard.isActive()) {
            throw new IllegalArgumentException("Исходная карта не активна");
        }

        if (!toCard.isActive()) {
            throw new IllegalArgumentException("Целевая карта не активна");
        }

        BigDecimal amount = requestDTO.getAmount();
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Недостаточно средств на карте");
        }

        if (amount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Минимальная сумма перевода: 1");
        }

        BigDecimal maxAmount = new BigDecimal("1000000");
        if (amount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("Максимальная сумма перевода: 1,000,000");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transfer = new Transfer();
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(amount);
        transfer.setDescription(requestDTO.getDescription());
        transfer.setStatus("COMPLETED");
        transfer.setCreatedAt(LocalDateTime.now());

        return transferRepository.save(transfer);
    }

    @Transactional(readOnly = true)
    public Transfer getTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Перевод не найден"));

        checkTransferAccess(transfer);

        return transfer;
    }

    @Transactional(readOnly = true)
    public Page<Transfer> getTransfersByCard(Long cardId, Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        if (!card.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedCardAccessException("Доступ к карте запрещен");
        }

        return transferRepository.findByCardId(cardId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Transfer> getUserTransfers(Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();
        return transferRepository.findByUserId(currentUserId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Transfer> getRecentTransfers(int limit) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Pageable pageable = Pageable.ofSize(limit);
        Page<Transfer> transfers = transferRepository.findByUserId(currentUserId, pageable);
        return transfers.getContent();
    }

    @Transactional(readOnly = true)
    public List<Transfer> getRecentTransfersForUser(Long userId, int limit) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Доступ запрещен. Требуются права администратора");
        }

        Pageable pageable = Pageable.ofSize(limit);
        Page<Transfer> transfers = transferRepository.findByUserId(userId, pageable);
        return transfers.getContent();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalTransferredAmount(Long cardId) {
        checkCardAccess(cardId);

        List<Transfer> outgoingTransfers = transferRepository.findByFromCardId(cardId);

        return outgoingTransfers.stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalReceivedAmount(Long cardId) {
        checkCardAccess(cardId);

        List<Transfer> incomingTransfers = transferRepository.findByToCardId(cardId);

        return incomingTransfers.stream()
                .map(Transfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void checkTransferAccess(Transfer transfer) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Long fromUserId = transfer.getFromCard().getUser().getId();
        Long toUserId = transfer.getToCard().getUser().getId();

        if (!currentUserId.equals(fromUserId) && !currentUserId.equals(toUserId)) {
            throw new UnauthorizedCardAccessException("Доступ к переводу запрещен");
        }
    }

    private void checkCardAccess(Long cardId) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        if (!card.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedCardAccessException("Доступ к карте запрещен");
        }
    }
}