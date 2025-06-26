package com.eduhub.auth_service.repository;

import com.eduhub.auth_service.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, String> {

    Mono<User> findByEmail(String email);
    @Query("SELECT * FROM users WHERE email = :email AND status = 'ACTIVE'")
    Mono<User> findActiveByEmail(String email);
    Mono<User> findById(UUID id);

    Mono<Boolean> existsByEmail(String email);
}