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

//    private String buildEmailContent(String otpCode) {
//        return """
//            <html>
//            <body>
//                <h2>Your Verification Code</h2>
//                <p>Please use the following code to verify your account:</p>
//                <h3 style="color: #2563eb;">%s</h3>
//                <p>This code will expire in 5 minutes.</p>
//                <p>If you didn't request this, please ignore this email.</p>
//            </body>
//            </html>
//            """.formatted(otpCode);
//    }

    private String buildEmailContent(String otpCode) {
        return """
        <html>
        <head>
            <style>
                body {
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #f9fafb;
                    margin: 0;
                    padding: 0;
                }
                .container {
                    max-width: 600px;
                    margin: 30px auto;
                    background: #ffffff;
                    border-radius: 12px;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
                    overflow: hidden;
                }
                .header {
                    background: linear-gradient(90deg, #2563eb, #1d4ed8);
                    color: #ffffff;
                    text-align: center;
                    padding: 20px;
                }
                .header h1 {
                    margin: 0;
                    font-size: 24px;
                    font-weight: 600;
                }
                .content {
                    padding: 30px;
                    text-align: center;
                }
                .content h2 {
                    color: #111827;
                    margin-bottom: 10px;
                }
                .otp-code {
                    display: inline-block;
                    background: #2563eb;
                    color: #ffffff;
                    font-size: 22px;
                    font-weight: bold;
                    letter-spacing: 3px;
                    padding: 12px 24px;
                    border-radius: 8px;
                    margin: 20px 0;
                }
                .footer {
                    font-size: 12px;
                    color: #6b7280;
                    text-align: center;
                    padding: 15px;
                    background: #f3f4f6;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>ProHand's</h1>
                </div>
                <div class="content">
                    <h2>Email Verification Code</h2>
                    <p>Please use the code below to verify your account. This code is valid for 5 minutes.</p>
                    <div class="otp-code">%s</div>
                    <p>If you did not request this code, you can safely ignore this email.</p>
                </div>
                <div class="footer">
                    © 2025 Your Company Name. All rights reserved.
                </div>
            </div>
        </body>
        </html>
        """.formatted(otpCode);
    }

}