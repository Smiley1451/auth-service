package com.eduhub.auth_service.exception;

public class MfaRequiredException extends RuntimeException {

    public MfaRequiredException(String message) {
        super(message);
    }

    public MfaRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}