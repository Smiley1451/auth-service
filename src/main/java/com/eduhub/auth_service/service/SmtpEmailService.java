package com.eduhub.auth_service.service;

import com.eduhub.auth_service.exception.EmailSendException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import lombok.extern.slf4j.Slf4j;

import jakarta.mail.internet.MimeMessage;
@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public Mono<Boolean> sendOtpEmail(String recipientEmail, String otpCode) {
        return Mono.fromCallable(() -> {
                    try {
                        MimeMessage message = mailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(message);

                        helper.setTo(recipientEmail);
                        helper.setSubject("Your Verification Code");
                        helper.setText(buildEmailContent(otpCode), true); // true = isHTML

                        mailSender.send(message);
                        return true;
                    } catch (Exception e) {
                        throw new EmailSendException("Failed to send OTP email", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(__ -> log.info("OTP email sent to {}", recipientEmail))
                .doOnError(e -> log.error("Failed to send OTP email to {}", recipientEmail, e));
    }

    private String buildEmailContent(String otpCode) {
        return """
            <html>
            <body>
                <h2>Your Verification Code</h2>
                <p>Please use the following code to verify your account:</p>
                <h3 style="color: #2563eb;">%s</h3>
                <p>This code will expire in 5 minutes.</p>
                <p>If you didn't request this, please ignore this email.</p>
            </body>
            </html>
            """.formatted(otpCode);
    }
}