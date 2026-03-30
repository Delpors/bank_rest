package com.example.bankcards.exception;

public class CardAlreadyActiveException extends RuntimeException{
    public CardAlreadyActiveException(String message){
        super(message);
    }
}
