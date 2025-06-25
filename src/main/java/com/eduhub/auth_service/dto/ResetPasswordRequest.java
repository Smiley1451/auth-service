package com.eduhub.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String otp;

    @NotBlank
    @Size(min = 8, max = 128)
    private String newPassword;
}