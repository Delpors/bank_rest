package com.example.bankcards.exception;

import com.example.bankcards.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardNotFoundException.class)
    ResponseEntity<ErrorResponse> handleCardNotFound(CardNotFoundException e){

        ErrorResponse error = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    ResponseEntity<ErrorResponse> handleUnAuthorizeException(UnauthorizedActionException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnAuthorizeException(Exception e){
        ErrorResponse error = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }


}
