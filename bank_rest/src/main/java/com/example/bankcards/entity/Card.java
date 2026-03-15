package com.example.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Card extends BaseEntity{

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false, unique = true)
    private String cardHolderName;

    @Column(nullable = false)
    private LocalDate expireDate;

    @Column(nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = true)
    private LocalDateTime blockedAt;

    @Column(nullable = true)
    private String blockReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Boolean isActive(){
        return CardStatus.ACTIVE.equals(this.status);
    }

    public Boolean isBlocked(){
        return CardStatus.BLOCKED.equals(this.status);
    }

    public void block(String blockReason){
        this.status = CardStatus.BLOCKED;
        this.blockedAt = LocalDateTime.now();
    }

    public void activate(){
        this.status = CardStatus.ACTIVE;
        this.blockedAt = null;
        this.blockReason = null;
    }

}
