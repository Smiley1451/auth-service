package com.eduhub.auth_service.service;

import com.eduhub.auth_service.constants.Role;
import com.eduhub.auth_service.constants.Status;
import com.eduhub.auth_service.dto.*;
import com.eduhub.auth_service.entity.User;
import com.eduhub.auth_service.exception.*;
import com.eduhub.auth_service.kafka.UserCreatedProducer;
import com.eduhub.auth_service.repository.UserRepository;
import com.eduhub.auth_service.util.OtpUtil;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.eduhub.auth_service.service.EmailService.*;

import jakarta.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Qualifier("authRedisOperations")
    private final ReactiveRedisOperations<String, String> redisOperations;
    private final UserCreatedProducer userCreatedProducer;
    private final OtpUtil otpUtil;
    private final EmailService emailService;

    private final Retry retry = Retry.of("kafka-retry", RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofSeconds(1))
            .build());
    public Mono<AuthResponse> signup(@Valid SignupRequest request) {
        request.normalize();
        return validateTeacherCreation(request)
                .then(Mono.defer(() -> userRepository.existsByEmail(request.getEmail())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new UserAlreadyExistsException("Email already in use"));
                            }

                            UUID userId = UUID.randomUUID();
                            return userRepository.saveWithTypeCasting(
                                            userId,
                                            request.getEmail(),
                                            passwordEncoder.encode(request.getPassword()),
                                            request.getRole().name(),
                                            Status.PENDING.name(),
                                            LocalDateTime.now(),
                                            LocalDateTime.now(),
                                            request.isMfaEnabled()
                                    )
                                    .flatMap(savedUser -> {
                                        String otp = otpUtil.generateOtp();
                                        return redisOperations.opsForValue()
                                                .set("otp:" + savedUser.getEmail(), otp, Duration.ofMinutes(5))
                                                .then(emailService.sendOtpEmail(savedUser.getEmail(), otp))
                                                .onErrorResume(e -> {
                                                    log.error("Failed to send OTP email: {}", e.getMessage());
                                                    return Mono.error(new EmailSendException("Failed to send OTP email", e));
                                                })
                                                .then(createSession(savedUser))
                                                .then(Mono.fromCallable(() ->
                                                        retry.executeSupplier(() ->
                                                                userCreatedProducer.sendUserCreatedEvent(savedUser, "EMAIL").block()
                                                        )
                                                ))
                                                .doOnSuccess(result ->
                                                        log.info("User created event sent for email: {}", savedUser.getEmail()))
                                                .doOnError(e ->
                                                        log.error("Failed to send user created event for email: {}", savedUser.getEmail(), e))
                                                .thenReturn(generateAuthResponse(savedUser));
                                    });
                        })));
    }


    public Mono<Void> resetPassword(@Valid ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        return redisOperations.opsForValue().get("reset:" + email)
                .switchIfEmpty(Mono.error(new InvalidOtpException("OTP expired or invalid")))
                .flatMap(otp -> {
                    if (!otp.equals(request.getOtp())) {
                        return Mono.error(new InvalidOtpException("Invalid OTP"));
                    }
                    return userRepository.findByEmail(email)
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                            .flatMap(user -> userRepository.updatePassword(
                                    user.getId(),
                                    passwordEncoder.encode(request.getNewPassword()),
                                    LocalDateTime.now()
                            ))
                            .then(redisOperations.delete("reset:" + email))
                            .doOnSuccess(result -> log.info("Password reset for user: {}", email))
                            .then();
                });
    }


    private Mono<Void> validateTeacherCreation(SignupRequest request) {
        if (request.getRole() == Role.TEACHER) {
            return ReactiveSecurityContextHolder.getContext()
                    .map(ctx -> ctx.getAuthentication())
                    .filter(auth -> auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
                    .switchIfEmpty(Mono.error(new AccessDeniedException("Only admins can create teacher accounts")))
                    .then();
        }
        return Mono.empty();
    }

    private Mono<Boolean> sendVerificationOtp(User user, String prefix) {
        String otp = otpUtil.generateOtp();
        return redisOperations.opsForValue()
                .set(prefix + ":" + user.getEmail(), otp, Duration.ofMinutes(5))
                .doOnSuccess(result -> log.info("OTP sent for {}: {}", user.getEmail(), otp))
                .thenReturn(true);
    }

    private Mono<Void> createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        return redisOperations.opsForValue()
                .set("session:" + user.getId(), sessionId, Duration.ofHours(24))
                .doOnSuccess(result -> log.info("Session created for user: {}", user.getId()))
                .then();
    }

    private AuthResponse generateAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateToken(
                        user.getEmail(),
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                ),
                user.getId().toString(),
                user.getRole(),
                user.getEmail(),
                jwtService.generateRefreshToken(user.getEmail())
        );
    }

    public Mono<AuthResponse> login(@Valid LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        return userRepository.findActiveByEmail(normalizedEmail)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid email or password")))
                .flatMap(user -> {
                    if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                        return Mono.error(new InvalidCredentialsException("Invalid email or password"));
                    }

                    if (user.isMfaEnabled()) {
                        String otp = otpUtil.generateOtp();
                        return redisOperations.opsForValue()
                                .set("otp:" + user.getEmail(), otp, Duration.ofMinutes(5))
                                .then(emailService.sendOtpEmail(user.getEmail(), otp))
                                .then(Mono.error(new MfaRequiredException("MFA OTP sent to email")));
                    }
                    return createSession(user)
                            .thenReturn(generateAuthResponse(user));
                });
    }

    public Mono<AuthResponse> verifyMfa(@Valid MfaRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        return redisOperations.opsForValue().get("otp:" + normalizedEmail)
                .switchIfEmpty(Mono.error(new InvalidOtpException("OTP expired or invalid")))
                .flatMap(storedOtp -> {
                    if (!storedOtp.equals(request.getOtp())) {
                        return Mono.error(new InvalidOtpException("Invalid OTP"));
                    }
                    return userRepository.findActiveByEmail(normalizedEmail)
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                            .flatMap(user -> {
                                // Clear OTP after successful verification
                                return redisOperations.delete("otp:" + normalizedEmail)
                                        .then(createSession(user))
                                        .thenReturn(generateAuthResponse(user))
                                        .doOnSuccess(response ->
                                                log.info("MFA verified for user: {}", user.getEmail()));
                            });
                });
    }

    public Mono<AuthResponse> refreshToken(@Valid RefreshTokenRequest request) {
        return jwtService.validateRefreshToken(request.getRefreshToken())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new InvalidTokenException("Invalid refresh token"));
                    }
                    return jwtService.getUsername(request.getRefreshToken())
                            .flatMap(username -> userRepository.findActiveByEmail(username)
                                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                                    .flatMap(user -> createSession(user)
                                            .thenReturn(generateAuthResponse(user))
                                            .doOnSuccess(response -> log.info("Token refreshed for user: {}", user.getEmail()))));
                });
    }

    public Mono<Void> requestPasswordReset(String email) {
        final String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                .flatMap(user -> sendVerificationOtp(user, "reset")
                        .doOnSuccess(result -> log.info("Password reset OTP sent for: {}", normalizedEmail))
                        .then());
    }



    public Mono<Void> logout(String token) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .flatMap(auth -> {
                    String email = auth.getPrincipal().toString();
                    return userRepository.findByEmail(email)
                            .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                            .flatMap(user -> jwtService.blacklistToken(token, user.getId())
                                    .then(redisOperations.delete("session:" + user.getId()))
                                    .doOnSuccess(result -> log.info("Session cleared for user: {}", user.getId())))
                            .then();
                });
    }

    public Mono<Void> verifyAccount(VerifyAccountRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        return redisOperations.opsForValue().get("otp:" + email)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new InvalidOtpException("OTP expired or invalid"))))
                .filter(otp -> otp.equals(request.getOtp()))
                .switchIfEmpty(Mono.error(new InvalidOtpException("Invalid OTP")))
                .flatMap(otp -> userRepository.findByEmail(email)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")))
                        .flatMap(user -> userRepository.updateStatus(
                                user.getId(),
                                Status.ACTIVE.name(),
                                LocalDateTime.now()
                        ))
                        .then(redisOperations.delete("otp:" + email))
                )
                .then()
                .doOnSuccess(__ -> log.info("Account verified: {}", email))
                .doOnError(e -> log.error("Verification error for {}: {}", email, e.getMessage()));
    }
}