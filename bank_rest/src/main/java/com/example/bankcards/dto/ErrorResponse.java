package com.example.bankcards.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String message;
    private final LocalDateTime time;

    public ErrorResponse(String message){
        this.message = message;
        this.time = LocalDateTime.now();
    }

}
