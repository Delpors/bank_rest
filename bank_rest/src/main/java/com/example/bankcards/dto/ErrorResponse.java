package com.example.bankcards.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ErrorResponse {
    private String message;
    private LocalDateTime time;
    private int status;

    public ErrorResponse(String message){
        this.message = message;
        this.time = LocalDateTime.now();
        this.status = HttpStatus.NOT_FOUND.value();
    }

}
