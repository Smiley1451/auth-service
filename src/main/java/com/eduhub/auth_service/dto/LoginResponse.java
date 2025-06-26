// Add this to your DTO package
package com.eduhub.auth_service.dto;

import com.eduhub.auth_service.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String userId;
    private Role role;
    private String email;
    private String refreshToken;
    private boolean mfaRequired;
    private String message;

    public static LoginResponse fromAuthResponse(AuthResponse authResponse) {
        return LoginResponse.builder()
                .token(authResponse.getToken())
                .userId(authResponse.getUserId())
                .role(authResponse.getRole())
                .email(authResponse.getEmail())
                .refreshToken(authResponse.getRefreshToken())
                .mfaRequired(false)
                .build();
    }

    public static LoginResponse mfaRequired(String message) {
        return LoginResponse.builder()
                .mfaRequired(true)
                .message(message)
                .build();
    }
}