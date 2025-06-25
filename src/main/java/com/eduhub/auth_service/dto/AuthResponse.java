package com.eduhub.auth_service.dto;

import com.eduhub.auth_service.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userId;
    private Role role;  // Added role field
    private String email; // Added email for client convenience
    private String refreshToken;
}