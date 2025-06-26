package com.eduhub.auth_service.service;

import com.eduhub.auth_service.entity.BlacklistedToken;
import com.eduhub.auth_service.repository.BlacklistedTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        SecretKey key = getSigningKey();
        return Jwts.builder()
                .subject(username)
                .claim("roles", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public Mono<Boolean> validateToken(String token) {
        try {
            return blacklistedTokenRepository.existsByTokenHash(token)
                    .map(exists -> !exists && !isTokenExpired(token));
        } catch (Exception e) {
            return Mono.just(false);
        }
    }

    public Mono<String> getUsername(String token) {
        return Mono.fromCallable(() -> getClaims(token).getSubject())
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<List<GrantedAuthority>> getAuthorities(String token) {
        return Mono.fromCallable(() -> {
            Claims claims = getClaims(token);
            List<String> roles = claims.get("roles", List.class);
            return roles.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                    .collect(Collectors.toList());
        }).onErrorResume(e -> Mono.empty());
    }

    private boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private Claims getClaims(String token) {
        SecretKey key = getSigningKey();
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateRefreshToken(String username) {
        SecretKey key = getSigningKey();
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 2))
                .signWith(key)
                .compact();
    }

    public Mono<Boolean> validateRefreshToken(String token) {
        return blacklistedTokenRepository.existsByTokenHash(token)
                .map(exists -> !exists && !isTokenExpired(token));
    }

    public Mono<Void> blacklistToken(String token, String userId) {
        return blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .id(UUID.randomUUID().toString())
                        .tokenHash(token)
                        .userId(userId)
                        .expiresAt(LocalDateTime.now().plusHours(2))
                        .build()
        ).then();
    }
}