package com.eduhub.auth_service.service;

import com.eduhub.auth_service.constants.OAuthProvider;
import com.eduhub.auth_service.constants.Role;
import com.eduhub.auth_service.constants.Status;
import com.eduhub.auth_service.dto.AuthResponse;
import com.eduhub.auth_service.dto.OAuth2UserInfo;
import com.eduhub.auth_service.entity.OAuthUser;
import com.eduhub.auth_service.entity.User;
import com.eduhub.auth_service.exception.AccountNotActiveException;
import com.eduhub.auth_service.kafka.UserCreatedProducer;
import com.eduhub.auth_service.repository.OAuthUserRepository;
import com.eduhub.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserRepository userRepository;
    private final OAuthUserRepository oAuthUserRepository;
    private final JwtService jwtService;
    private final UserCreatedProducer userCreatedProducer;

    public Mono<AuthResponse> processOAuth2User(OAuth2UserInfo oAuth2UserInfo) {
        return oAuthUserRepository.findByProviderAndExternalId(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getId())
                .flatMap(oAuthUser -> userRepository.findById(UUID.fromString(oAuthUser.getUserId())))
                .switchIfEmpty(Mono.defer(() -> registerNewOAuth2User(oAuth2UserInfo)))
                .flatMap(user -> {
                    if (user.getStatus() != Status.ACTIVE) {
                        return Mono.error(new AccountNotActiveException("Account not active"));
                    }
                    return Mono.just(generateAuthResponse(user));
                });
    }

    private Mono<User> registerNewOAuth2User(OAuth2UserInfo oAuth2UserInfo) {
        return userRepository.findByEmail(oAuth2UserInfo.getEmail())
                .switchIfEmpty(Mono.defer(() -> createNewUserFromOAuth(oAuth2UserInfo)))
                .flatMap(user -> {
                    OAuthUser oAuthUser = OAuthUser.builder()
                            .id(UUID.randomUUID().toString())
                            .provider(oAuth2UserInfo.getProvider())
                            .externalId(oAuth2UserInfo.getId())
                            .userId(user.getId().toString())
                            .createdAt(LocalDateTime.now())
                            .build();

                    return oAuthUserRepository.save(oAuthUser)
                            .thenReturn(user);
                });
    }

    private Mono<User> createNewUserFromOAuth(OAuth2UserInfo oAuth2UserInfo) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(oAuth2UserInfo.getEmail())
                .passwordHash(null)
                .role(Role.STUDENT)
                .status(Status.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user)
                .flatMap(savedUser -> userCreatedProducer.sendUserCreatedEvent(
                                savedUser,
                                "OAUTH_" + oAuth2UserInfo.getProvider().name())
                        .thenReturn(savedUser));
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateToken(
                user.getEmail(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new AuthResponse(
                accessToken,
                user.getId().toString(),
                user.getRole(),
                user.getEmail(),
                refreshToken
        );
    }
}