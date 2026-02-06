package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedCardAccessException extends RuntimeException {

    public UnauthorizedCardAccessException(String message) {
        super(message);
    }

    public UnauthorizedCardAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}