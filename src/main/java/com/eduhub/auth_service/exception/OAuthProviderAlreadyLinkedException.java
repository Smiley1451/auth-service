package com.eduhub.auth_service.exception;

public class OAuthProviderAlreadyLinkedException extends RuntimeException {
    public OAuthProviderAlreadyLinkedException(String message) {
        super(message);
    }
}