package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 500)
    @Column(name = "card_number_encrypted", nullable = false)
    private String cardNumberEncrypted;

    @NotBlank
    @Size(max = 100)
    @Column(name = "card_number_masked", nullable = false)
    private String cardNumberMasked;

    @NotBlank
    @Size(max = 255)
    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Card() {
    }

    public Card(String cardNumberEncrypted, String cardNumberMasked, String holderName,
                LocalDate expiryDate, User user) {
        this.cardNumberEncrypted = cardNumberEncrypted;
        this.cardNumberMasked = cardNumberMasked;
        this.holderName = holderName;
        this.expiryDate = expiryDate;
        this.user = user;
        this.status = CardStatus.ACTIVE;
        this.balance = BigDecimal.ZERO;
    }

    public boolean isActive() {
        return status == CardStatus.ACTIVE && !isExpired();
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public void updateStatus() {
        if (status != CardStatus.BLOCKED) {
            if (isExpired()) {
                status = CardStatus.EXPIRED;
            } else {
                status = CardStatus.ACTIVE;
            }
        }
    }

    public boolean canTransfer(BigDecimal amount) {
        return isActive() && balance.compareTo(amount) >= 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardNumberEncrypted() {
        return cardNumberEncrypted;
    }

    public void setCardNumberEncrypted(String cardNumberEncrypted) {
        this.cardNumberEncrypted = cardNumberEncrypted;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public void setCardNumberMasked(String cardNumberMasked) {
        this.cardNumberMasked = cardNumberMasked;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public CardStatus getStatus() {
        updateStatus(); // Обновляем статус при получении
        return status;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        updateStatus();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateStatus();
    }
}