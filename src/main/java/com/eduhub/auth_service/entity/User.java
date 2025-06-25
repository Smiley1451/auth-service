package com.eduhub.auth_service.entity;

import com.eduhub.auth_service.constants.Role;
import com.eduhub.auth_service.constants.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column("id")
    private String id;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be empty")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column("email")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    @Column("password_hash")
    private String passwordHash;

    @Column("role")
    private Role role;

    @Column("status")
    private
    Status status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    private boolean mfaEnabled;
}