package com.eduhub.auth_service.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "blacklisted_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistedToken {

    @Id
    @Column("id")
    private String id;

    @NotBlank(message = "Token hash cannot be empty")
    @Column("token_hash")
    private String tokenHash;

    @NotNull(message = "Expiration date cannot be null")
    @Column("expires_at")
    private LocalDateTime expiresAt;

    @NotBlank(message = "User ID cannot be empty")
    @Column("user_id")
    private String userId;
}