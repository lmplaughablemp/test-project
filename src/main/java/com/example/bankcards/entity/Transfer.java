package com.example.bankcards.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private Card fromCard;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private Card toCard;

    @NotNull
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Size(max = 500)
    private String description;

    @Column(nullable = false, length = 20)
    private String status = "COMPLETED"; // COMPLETED, FAILED, PENDING

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    public Transfer() {
    }

    public Transfer(Card fromCard, Card toCard, BigDecimal amount, String description) {
        this.fromCard = fromCard;
        this.toCard = toCard;
        this.amount = amount;
        this.description = description;
        this.status = "COMPLETED";
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Card getFromCard() {
        return fromCard;
    }

    public void setFromCard(Card fromCard) {
        this.fromCard = fromCard;
    }

    public Card getToCard() {
        return toCard;
    }

    public void setToCard(Card toCard) {
        this.toCard = toCard;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}