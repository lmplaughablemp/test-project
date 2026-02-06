package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByUserId(Long userId);

    Page<Card> findByUserId(Long userId, Pageable pageable);

    Optional<Card> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND " +
            "(:status IS NULL OR c.status = :status) AND " +
            "(:search IS NULL OR LOWER(c.holderName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "c.cardNumberMasked LIKE CONCAT('%', :search, '%'))")
    Page<Card> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("status") CardStatus status,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    List<Card> findActiveCardsByUserId(@Param("userId") Long userId);

    Optional<Card> findByCardNumberEncrypted(String cardNumberEncrypted);

    boolean existsByCardNumberEncrypted(String cardNumberEncrypted);

    @Query("SELECT COUNT(c) > 0 FROM Card c WHERE c.id = :cardId AND c.user.id = :userId")
    boolean existsByIdAndUserId(@Param("cardId") Long cardId, @Param("userId") Long userId);

    @Query("SELECT c.balance FROM Card c WHERE c.id = :cardId")
    Optional<BigDecimal> findBalanceById(@Param("cardId") Long cardId);

    @Query("SELECT c FROM Card c WHERE c.id = :cardId AND c.user.id = :userId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)  // Исправлено: вместо FOR UPDATE используем аннотацию
    Optional<Card> findByIdAndUserIdForUpdate(@Param("cardId") Long cardId, @Param("userId") Long userId);
}