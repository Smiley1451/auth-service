package com.eduhub.auth_service.service;

import reactor.core.publisher.Mono;

public interface EmailService {
    Mono<Boolean> sendOtpEmail(String recipientEmail, String otpCode);
}