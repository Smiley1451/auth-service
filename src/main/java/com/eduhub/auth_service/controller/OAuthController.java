//package com.eduhub.auth_service.controller;
//
//import com.eduhub.auth_service.dto.AuthResponse;
//import com.eduhub.auth_service.exception.*;
//import com.eduhub.auth_service.service.OAuthService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//
//import java.util.Collections;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth/oauth")
//@RequiredArgsConstructor
//@Slf4j
//public class OAuthController {
//
//    private final OAuthService oAuthService;
//
//    @GetMapping("/user")
//    public Mono<ResponseEntity<Map<String, Object>>> getOAuthUserInfo(
//            @AuthenticationPrincipal OAuth2User principal) {
//        if (principal == null) {
//            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                    .body(Collections.singletonMap("error", "Not authenticated")));
//        }
//        return Mono.just(ResponseEntity.ok(principal.getAttributes()));
//    }
//
//    @GetMapping("/success")
//    public Mono<ResponseEntity<AuthResponse>> handleOAuthSuccess(
//            @AuthenticationPrincipal OAuth2User oAuth2User) {
//        return oAuthService.getAuthResponse(oAuth2User)
//                .map(ResponseEntity::ok)
//                .onErrorResume(OAuth2AuthenticationProcessingException.class, e -> {
//                    log.error("OAuth processing error: {}", e.getMessage());
//                    return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                            .body(new AuthResponse(null, null, null, null, e.getMessage())));
//                })
//                .onErrorResume(UserNotFoundException.class, e -> {
//                    log.error("User not found after OAuth success: {}", e.getMessage());
//                    return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
//                            .body(new AuthResponse(null, null, null, null, e.getMessage())));
//                });
//    }
//
//    @GetMapping("/providers")
//    public Mono<ResponseEntity<Map<String, String>>> getAvailableOAuthProviders() {
//        return Mono.just(ResponseEntity.ok(Map.of(
//                "google", "/oauth2/authorization/google",
//                "github", "/oauth2/authorization/github"
//        )));
//    }
//
//    @ExceptionHandler(OAuthProviderAlreadyLinkedException.class)
//    public Mono<ResponseEntity<Map<String, String>>> handleOAuthProviderAlreadyLinked(
//            OAuthProviderAlreadyLinkedException ex) {
//        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
//                .body(Collections.singletonMap("error", ex.getMessage())));
//    }
//
//    @ExceptionHandler(EmailAlreadyInUseException.class)
//    public Mono<ResponseEntity<Map<String, String>>> handleEmailAlreadyInUse(
//            EmailAlreadyInUseException ex) {
//        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
//                .body(Collections.singletonMap("error", ex.getMessage())));
//    }
//
//    @ExceptionHandler(OAuth2AuthenticationProcessingException.class)
//    public Mono<ResponseEntity<Map<String, String>>> handleOAuthProcessingException(
//            OAuth2AuthenticationProcessingException ex) {
//        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(Collections.singletonMap("error", ex.getMessage())));
//    }
//}