package com.eduhub.auth_service.repository;

import com.eduhub.auth_service.constants.OAuthProvider;
import com.eduhub.auth_service.entity.OAuthUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface OAuthUserRepository extends ReactiveCrudRepository<OAuthUser, String> {
   // Mono<OAuthUser> findByProviderAndExternalId(OAuthProvider provider, String externalId);
    Mono<Boolean> existsByUserId(String userId);
    Mono<OAuthUser> findByProviderAndExternalId(OAuthProvider provider, String externalId);
}
