package com.example.bankcards.exception;

public class UnauthorizedActionException extends RuntimeException{
    public UnauthorizedActionException(String message){
        super(message);
    }
}
