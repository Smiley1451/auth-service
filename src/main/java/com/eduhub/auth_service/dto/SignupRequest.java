package com.eduhub.auth_service.dto;

import com.eduhub.auth_service.constants.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data Transfer Object for user signup requests.
 * Contains email, password, role, and MFA preference for creating a new user account.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for user signup")
public class SignupRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Email(message = "Email must be valid")
    @NotNull(message = "Email cannot be null")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @NotNull(message = "Password cannot be null")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    @Schema(description = "User's password (min 8 chars, with uppercase, lowercase, number, and special character)", example = "P@ssw0rd!")
    private String password;

    @NotNull(message = "Role cannot be null")
    @Schema(description = "User's role", example = "STUDENT", allowableValues = {"STUDENT", "TEACHER", "ADMIN"})
    private Role role;

    @Schema(description = "Enable Multi-Factor Authentication", example = "true")
    private boolean mfaEnabled;

    /**
     * Normalizes email to lowercase and trims whitespace from fields.
     */
    public void normalize() {
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (password != null) {
            password = password.trim();
        }
    }
}