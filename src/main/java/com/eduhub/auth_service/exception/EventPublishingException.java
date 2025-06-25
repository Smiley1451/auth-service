package com.eduhub.auth_service.exception;

public class EventPublishingException extends RuntimeException {
    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
}