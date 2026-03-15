package com.example.bankcards.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException{
    public InsufficientFundsException(String message){
        super(message);
    }

    public InsufficientFundsException(Long cardId, BigDecimal balance, BigDecimal amount) {
        super(String.format("Недостаточно средств на карте %d. Баланс: %.2f, запрошено: %.2f",
                cardId, balance, amount));
    }
}
