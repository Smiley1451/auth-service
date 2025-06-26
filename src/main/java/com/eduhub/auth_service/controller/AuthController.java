package com.eduhub.auth_service.controller;

import com.eduhub.auth_service.dto.*;
import com.eduhub.auth_service.exception.InvalidOtpException;
import com.eduhub.auth_service.exception.MfaRequiredException;
import com.eduhub.auth_service.exception.ResourceNotFoundException;
import com.eduhub.auth_service.service.AuthService;
import com.eduhub.auth_service.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user authentication and authorization")
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with optional MFA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already in use"),
            @ApiResponse(responseCode = "403", description = "Only admins can create teacher accounts")
    })
    @PostMapping("/signup")
    public Mono<ResponseEntity<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Processing signup request for email: {}", request.getEmail());
        return authService.signup(request)
                .map(response -> ResponseEntity.ok(response))
                .doOnError(e -> log.error("Signup failed for email: {}", request.getEmail(), e));
    }




    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token or triggers MFA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "202", description = "MFA OTP sent to email"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());
        request.normalize();
        return authService.login(request)
                .flatMap(authResponse ->
                        Mono.just(ResponseEntity.ok(LoginResponse.fromAuthResponse(authResponse)))
                )
                .onErrorResume(MfaRequiredException.class, e ->
                        Mono.just(ResponseEntity.ok(LoginResponse.mfaRequired(e.getMessage())))
                )
                .doOnError(e -> log.error("Login failed for email: {}", request.getEmail(), e));
    }





    @Operation(summary = "Verify MFA OTP", description = "Verifies the OTP for MFA-enabled users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "MFA verified",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/mfa")
    public Mono<ResponseEntity<LoginResponse>> verifyMfa(@Valid @RequestBody MfaRequest request) {
        log.info("Processing MFA verification for email: {}", request.getEmail());
        return authService.verifyMfa(request)
                .map(authResponse -> ResponseEntity.ok(LoginResponse.fromAuthResponse(authResponse)))
                .doOnError(e -> log.error("MFA verification failed for email: {}", request.getEmail(), e));
    }




    @Operation(summary = "Refresh JWT token", description = "Generates a new JWT token using a refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public Mono<ResponseEntity<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Processing token refresh request");
        return authService.refreshToken(request)
                .map(response -> ResponseEntity.ok(response))
                .doOnError(e -> log.error("Token refresh failed", e));
    }




    @Operation(summary = "Request password reset", description = "Sends a password reset OTP to the user's email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset OTP sent"),
            @ApiResponse(responseCode = "400", description = "Invalid email"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/reset-password-request")
    public Mono<ResponseEntity<Void>> requestPasswordReset(
            @RequestParam @NotBlank(message = "Email cannot be blank") @Email(message = "Email must be valid") String email) {
        log.info("Processing password reset request for email: {}", email);
        return authService.requestPasswordReset(email)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .doOnError(e -> log.error("Password reset request failed for email: {}", email, e));
    }




    @Operation(summary = "Reset password", description = "Resets the user's password using the OTP")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    @PostMapping("/reset-password")
    public Mono<ResponseEntity<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Processing password reset for email: {}", request.getEmail());
        return authService.resetPassword(request)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .doOnError(e -> log.error("Password reset failed for email: {}", request.getEmail(), e));
    }




    @Operation(summary = "Logout", description = "Blacklists the JWT token and clears the user session")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        log.info("Processing logout request for token: {}", token.substring(0, Math.min(token.length(), 10)));
        return authService.logout(token)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .doOnError(e -> log.error("Logout failed for token: {}", token.substring(0, Math.min(token.length(), 10)), e));
    }




//    @Operation(summary = "OAuth2 callback", description = "Handles OAuth2 login callback for Google/GitHub")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "OAuth2 login successful",
//                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
//            @ApiResponse(responseCode = "400", description = "OAuth2 authentication failed")
//    })
//    @GetMapping("/oauth2/callback")
//    public Mono<ResponseEntity<AuthResponse>> oauth2Callback(@AuthenticationPrincipal OAuth2User oauth2User) {
//        String email = oauth2User.getAttribute("email") != null ? oauth2User.getAttribute("email") : "unknown";
//        log.info("Processing OAuth2 callback for user: {}", email);
//        OAuth2UserInfo userInfo = OAuth2UserInfo.fromOAuth2User(oauth2User);
//        return oAuthService.processOAuth2User(userInfo)
//                .map(response -> ResponseEntity.ok(response))
//                .doOnError(e -> log.error("OAuth2 callback failed for email: {}", email, e));
//    }




    @Operation(summary = "Verify account with OTP",
            description = "Verifies a user's account using the OTP sent to their email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/verify")
    public Mono<ResponseEntity<Void>> verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
        return authService.verifyAccount(request)
                .then(Mono.fromCallable(() -> {
                    log.info("Account verified successfully: {}", request.getEmail());
                    return ResponseEntity.ok().<Void>build();
                }))
                .onErrorResume(e -> {
                    log.error("Verification failed for {}: {}", request.getEmail(), e.getMessage());
                    return Mono.just(
                            e instanceof InvalidOtpException ? ResponseEntity.badRequest().build() :
                                    e instanceof ResourceNotFoundException ? ResponseEntity.notFound().build() :
                                            ResponseEntity.internalServerError().build()
                    );
                });
    }
}