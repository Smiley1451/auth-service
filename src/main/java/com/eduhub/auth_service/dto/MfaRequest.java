package com.eduhub.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaRequest {
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "OTP cannot be blank")
    private String otp;

    public void normalize() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (otp != null) {
            otp = otp.trim();
        }
    }
}