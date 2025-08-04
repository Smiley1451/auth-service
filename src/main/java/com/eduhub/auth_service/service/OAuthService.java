//package com.eduhub.auth_service.service;
//
//import com.eduhub.auth_service.config.OAuth2Config;
//import com.eduhub.auth_service.constants.OAuthProvider;
//import com.eduhub.auth_service.constants.Role;
//import com.eduhub.auth_service.constants.Status;
//import com.eduhub.auth_service.dto.AuthResponse;
//import com.eduhub.auth_service.entity.OAuthUser;
//import com.eduhub.auth_service.entity.User;
//import com.eduhub.auth_service.exception.*;
//import com.eduhub.auth_service.repository.OAuthUserRepository;
//import com.eduhub.auth_service.repository.UserRepository;
//import com.eduhub.auth_service.security.OAuth2UserInfo;
//import com.eduhub.auth_service.security.OAuth2UserInfoFactory;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import reactor.core.publisher.Mono;
//
//import java.util.Collections;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class OAuthService extends DefaultReactiveOAuth2UserService {
//
//    private final UserRepository userRepository;
//    private final OAuthUserRepository oAuthUserRepository;
//    private final JwtService jwtService;
//    private final OAuth2Config oAuth2Config;
//
//    @Override
//    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        log.info("Loading user for provider: {}", userRequest.getClientRegistration().getRegistrationId());
//        validateProviderConfiguration(userRequest.getClientRegistration().getRegistrationId());
//
//        return super.loadUser(userRequest)
//                .flatMap(oAuth2User -> processOAuth2User(userRequest, oAuth2User))
//                .onErrorResume(e -> {
//                    log.error("OAuth authentication failed: {}", e.getMessage());
//                    return Mono.error(new OAuth2AuthenticationException("OAuth authentication failed"));
//                });
//    }
//
//    private void validateProviderConfiguration(String providerId) {
//        switch (providerId.toLowerCase()) {
//            case "google":
//                if (oAuth2Config.getGoogle() == null || !StringUtils.hasText(oAuth2Config.getGoogle().getClientId())) {
//                    throw new OAuth2AuthenticationException("Google OAuth configuration is missing");
//                }
//                break;
//            case "github":
//                if (oAuth2Config.getGithub() == null || !StringUtils.hasText(oAuth2Config.getGithub().getClientId())) {
//                    throw new OAuth2AuthenticationException("GitHub OAuth configuration is missing");
//                }
//                break;
//            default:
//                throw new OAuth2AuthenticationException("Unsupported OAuth provider: " + providerId);
//        }
//    }
//
//    private Mono<OAuth2User> processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
//        String providerId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
//        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
//                providerId,
//                oAuth2User.getAttributes()
//        );
//
//        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
//            return Mono.error(new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider"));
//        }
//
//        return oAuthUserRepository.findByProviderAndExternalId(
//                        OAuthProvider.valueOf(providerId.toUpperCase()),
//                        oAuth2UserInfo.getId()
//                )
//                .flatMap(oauthUser -> handleExistingOAuthUser(oauthUser, oAuth2UserInfo))
//                .switchIfEmpty(Mono.defer(() -> handleNewOAuthUser(oAuth2UserRequest, oAuth2UserInfo)));
//    }
//
//    private Mono<OAuth2User> handleExistingOAuthUser(OAuthUser oauthUser, OAuth2UserInfo oAuth2UserInfo) {
//        return userRepository.findById(UUID.fromString(oauthUser.getUserId()))
//                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found for OAuth account")))
//                .flatMap(user -> {
//                    if (!user.getEmail().equals(oAuth2UserInfo.getEmail())) {
//                        log.warn("OAuth email changed for user {}: {} -> {}",
//                                user.getId(), user.getEmail(), oAuth2UserInfo.getEmail());
//                        return handleEmailChange(user, oAuth2UserInfo.getEmail());
//                    }
//                    return Mono.just(user);
//                })
//                .map(this::createOAuth2User);
//    }
//
//    private Mono<OAuth2User> handleNewOAuthUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
//        return userRepository.findByEmail(oAuth2UserInfo.getEmail())
//                .flatMap(existingUser -> {
//                    if (existingUser.getPasswordHash() != null) {
//                        log.info("Linking OAuth provider to existing manual account: {}", existingUser.getEmail());
//                        return handleManualUserLinking(oAuth2UserRequest, oAuth2UserInfo, existingUser);
//                    }
//                    return linkOAuthProvider(oAuth2UserRequest, oAuth2UserInfo, existingUser);
//                })
//                .switchIfEmpty(Mono.defer(() -> {
//                    log.info("Registering new OAuth user: {}", oAuth2UserInfo.getEmail());
//                    return registerNewOAuthUser(oAuth2UserRequest, oAuth2UserInfo);
//                }));
//    }
//
//    private Mono<OAuth2User> handleManualUserLinking(OAuth2UserRequest oAuth2UserRequest,
//                                                     OAuth2UserInfo oAuth2UserInfo,
//                                                     User existingUser) {
//        return oAuthUserRepository.findByUserIdAndProvider(
//                        existingUser.getId().toString(),
//                        OAuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()))
//                .flatMap(existingOAuth -> Mono.error(new OAuthProviderAlreadyLinkedException(
//                        "This OAuth provider is already linked to your account")))
//                .switchIfEmpty(Mono.defer(() ->
//                        linkOAuthProvider(oAuth2UserRequest, oAuth2UserInfo, existingUser)
//                ))
//                .cast(OAuth2User.class);
//    }
//
//    private Mono<OAuth2User> linkOAuthProvider(OAuth2UserRequest oAuth2UserRequest,
//                                               OAuth2UserInfo oAuth2UserInfo,
//                                               User user) {
//        OAuthUser oauthUser = OAuthUser.builder()
//                .id(UUID.randomUUID().toString())
//                .provider(OAuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()))
//                .externalId(oAuth2UserInfo.getId())
//                .userId(user.getId().toString())
//                .createdAt(LocalDateTime.now())
//                .build();
//
//        return oAuthUserRepository.save(oauthUser)
//                .thenReturn(createOAuth2User(user));
//    }
//
//    private Mono<User> handleEmailChange(User user, String newEmail) {
//        return userRepository.findByEmail(newEmail)
//                .flatMap(existingUser -> Mono.error(new EmailAlreadyInUseException(
//                        "This email is already associated with another account")))
//                .switchIfEmpty(Mono.defer(() -> {
//                    user.setEmail(newEmail);
//                    user.setUpdatedAt(LocalDateTime.now());
//                    return userRepository.save(user);
//                }))
//                .cast(User.class);
//    }
//
//    private Mono<OAuth2User> registerNewOAuthUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
//        User user = User.builder()
//                .id(UUID.randomUUID())
//                .email(oAuth2UserInfo.getEmail())
//                .role(Role.STUDENT)
//                .status(Status.ACTIVE)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .mfaEnabled(false)
//                .build();
//
//        return userRepository.saveWithTypeCasting(
//                        user.getId(),
//                        user.getEmail(),
//                        null,
//                        user.getRole().name(),
//                        user.getStatus().name(),
//                        user.getCreatedAt(),
//                        user.getUpdatedAt(),
//                        user.isMfaEnabled()
//                )
//                .flatMap(savedUser -> linkOAuthProvider(oAuth2UserRequest, oAuth2UserInfo, savedUser));
//    }
//
//    private OAuth2User createOAuth2User(User user) {
//        return new DefaultOAuth2User(
//                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
//                Collections.singletonMap("email", user.getEmail()),
//                "email"
//        );
//    }
//
//    public Mono<AuthResponse> getAuthResponse(OAuth2User oAuth2User) {
//        String email = oAuth2User.getAttribute("email");
//        return userRepository.findByEmail(email)
//                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found")))
//                .flatMap(user -> Mono.just(new AuthResponse(
//                        jwtService.generateToken(
//                                user.getEmail(),
//                                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
//                        ),
//                        user.getId().toString(),
//                        user.getRole(),
//                        user.getEmail(),
//                        jwtService.generateRefreshToken(user.getEmail())
//                )));
//    }
//}