package com.eduhub.auth_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotNull
    private String refreshToken;
}
