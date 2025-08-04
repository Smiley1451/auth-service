package com.eduhub.auth_service.repository;

import com.eduhub.auth_service.entity.BlacklistedToken;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface BlacklistedTokenRepository extends ReactiveCrudRepository<BlacklistedToken, String> {

    @Query("SELECT EXISTS(SELECT 1 FROM blacklisted_tokens WHERE token_hash = :tokenHash)")
    Mono<Boolean> existsByTokenHash(String tokenHash);

    @Query("DELETE FROM blacklisted_tokens WHERE expires_at < NOW()")
    Mono<Void> deleteExpiredTokens();

}