package com.eduhub.auth_service.repository;

import com.eduhub.auth_service.constants.OAuthProvider;
import com.eduhub.auth_service.entity.OAuthUser;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface OAuthUserRepository extends ReactiveCrudRepository<OAuthUser, String> {

    @Query("SELECT * FROM oauth_users WHERE provider = :provider::provider AND external_id = :externalId")
    Mono<OAuthUser> findByProviderAndExternalId(
            @Param("provider") OAuthProvider provider,
            @Param("externalId") String externalId
    );

    @Query("SELECT * FROM oauth_users WHERE user_id = :userId AND provider = :provider::provider")
    Mono<OAuthUser> findByUserIdAndProvider(
            @Param("userId") String userId,
            @Param("provider") OAuthProvider provider
    );
}