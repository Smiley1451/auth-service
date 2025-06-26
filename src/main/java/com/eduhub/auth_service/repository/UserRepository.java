package com.eduhub.auth_service.repository;

import com.eduhub.auth_service.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, String> {

    @Query("SELECT * FROM users WHERE email = :email")
    Mono<User> findByEmail(String email);

    @Query("SELECT * FROM users WHERE email = :email AND status = 'ACTIVE'::status")
    Mono<User> findActiveByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :id")
    Mono<User> findById(UUID id);

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)")
    Mono<Boolean> existsByEmail(String email);

    @Query("""
        INSERT INTO users (id, email, password_hash, role, status, created_at, updated_at, mfa_enabled)
        VALUES (:id, :email, :passwordHash, :role::role, :status::status, :createdAt, :updatedAt, :mfaEnabled)
        RETURNING *
        """)
    Mono<User> saveWithTypeCasting(
            @Param("id") UUID id,
            @Param("email") String email,
            @Param("passwordHash") String passwordHash,
            @Param("role") String role,
            @Param("status") String status,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("mfaEnabled") boolean mfaEnabled
    );

    @Query("""
        UPDATE users 
        SET password_hash = :passwordHash,
            updated_at = :updatedAt
        WHERE id = :id
        RETURNING *
        """)
    Mono<User> updatePassword(
            @Param("id") UUID id,
            @Param("passwordHash") String passwordHash,
            @Param("updatedAt") LocalDateTime updatedAt
    );

    @Query("UPDATE users SET status = :status::status, updated_at = :updatedAt WHERE id = :id")
    Mono<Void> updateStatus(
            @Param("id") UUID id,
            @Param("status") String status,
            @Param("updatedAt") LocalDateTime updatedAt
    );
}