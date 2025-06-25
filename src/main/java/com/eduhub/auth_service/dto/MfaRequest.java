package com.eduhub.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MfaRequest {
    @NotNull
    @Email
    private String email;
    @NotNull @Size(min = 6, max = 6) private String otp;
}