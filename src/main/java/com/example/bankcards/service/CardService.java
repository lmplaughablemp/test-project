package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateDTO;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UnauthorizedCardAccessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.CardNumberEncryptor;
import com.example.bankcards.util.CardNumberMasker;
import com.example.bankcards.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardNumberEncryptor cardNumberEncryptor;
    private final CardNumberMasker cardNumberMasker;
    private final SecurityUtils securityUtils;

    public CardService(CardRepository cardRepository,
                       CardNumberEncryptor cardNumberEncryptor,
                       CardNumberMasker cardNumberMasker,
                       SecurityUtils securityUtils) {
        this.cardRepository = cardRepository;
        this.cardNumberEncryptor = cardNumberEncryptor;
        this.cardNumberMasker = cardNumberMasker;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public CardDTO createCard(CardCreateDTO cardCreateDTO, Long userId) {
        if (!cardNumberMasker.isValidCardNumber(cardCreateDTO.getCardNumber())) {
            throw new IllegalArgumentException("Неверный номер карты");
        }

        if (cardCreateDTO.getExpiryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Срок действия карты истек");
        }

        String encryptedCardNumber = cardNumberEncryptor.encrypt(cardCreateDTO.getCardNumber());

        if (cardRepository.existsByCardNumberEncrypted(encryptedCardNumber)) {
            throw new IllegalArgumentException("Карта с таким номером уже существует");
        }

        String maskedCardNumber = cardNumberMasker.mask(cardCreateDTO.getCardNumber());

        User currentUser = securityUtils.getCurrentUser();

        User user = currentUser;
        if (userId != null && securityUtils.isAdmin()) {
        }

        Card card = new Card();
        card.setCardNumberEncrypted(encryptedCardNumber);
        card.setCardNumberMasked(maskedCardNumber);
        card.setHolderName(cardCreateDTO.getHolderName());
        card.setExpiryDate(cardCreateDTO.getExpiryDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);

        return convertToDTO(savedCard);
    }

    @Transactional(readOnly = true)
    public CardDTO getCardById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        checkCardAccess(card);

        return convertToDTO(card);
    }

    @Transactional(readOnly = true)
    public Page<CardDTO> getUserCards(Pageable pageable, CardStatus status, String search) {
        User currentUser = securityUtils.getCurrentUser();

        Page<Card> cards = cardRepository.findByUserIdWithFilters(
                currentUser.getId(), status, search, pageable);

        return cards.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CardDTO> getAllCards(Pageable pageable, CardStatus status, String search) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Доступ запрещен");
        }

        Page<Card> cards = cardRepository.findAll(pageable);

        return cards.map(this::convertToDTO);
    }

    @Transactional
    public CardDTO updateCardStatus(Long id, CardStatus newStatus) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        checkCardAccess(card);

        card.setStatus(newStatus);
        Card updatedCard = cardRepository.save(card);

        return convertToDTO(updatedCard);
    }

    @Transactional
    public void deleteCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        // Только администратор или владелец может удалить карту
        boolean isAdmin = securityUtils.isAdmin();
        boolean isOwner = card.getUser().getId().equals(securityUtils.getCurrentUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Доступ запрещен");
        }

        cardRepository.delete(card);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));

        checkCardAccess(card);

        return card.getBalance();
    }

    @Transactional
    public CardDTO blockCard(Long id) {
        return updateCardStatus(id, CardStatus.BLOCKED);
    }

    @Transactional
    public CardDTO activateCard(Long id) {
        return updateCardStatus(id, CardStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<CardDTO> getUserActiveCards() {
        Long userId = securityUtils.getCurrentUserId();

        List<Card> cards = cardRepository.findActiveCardsByUserId(userId);

        return cards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void checkCardAccess(Card card) {
        boolean isAdmin = securityUtils.isAdmin();
        boolean isOwner = card.getUser().getId().equals(securityUtils.getCurrentUserId());

        if (!isAdmin && !isOwner) {
            throw new UnauthorizedCardAccessException("Доступ к карте запрещен");
        }
    }

    private CardDTO convertToDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setCardNumberMasked(card.getCardNumberMasked());
        dto.setHolderName(card.getHolderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setUserId(card.getUser().getId());
        return dto;
    }
}