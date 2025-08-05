package com.eduhub.auth_service.kafka;

import com.eduhub.auth_service.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String userId;
    private String userName;
    private String email;
    private Role role;
    private Instant createdAt;
    private String source;
}