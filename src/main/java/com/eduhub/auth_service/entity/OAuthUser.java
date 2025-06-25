package com.eduhub.auth_service.entity;

import com.eduhub.auth_service.constants.OAuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "oauth_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthUser {

    @Id
    @Column("id")
    private String id;

    @NotNull(message = "OAuth provider cannot be null")
    @Column("provider")
    private OAuthProvider provider;

    @NotBlank(message = "External ID cannot be empty")
    @Column("external_id")
    private String externalId;

    @NotBlank(message = "User ID cannot be empty")
    @Column("user_id")
    private String userId;

    @Column("created_at")
    private LocalDateTime createdAt;
}