package com.eduhub.auth_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OAuth2AuthenticationProcessingException extends RuntimeException {
    public OAuth2AuthenticationProcessingException(String message) {
        super(message);
    }

    public OAuth2AuthenticationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}