package com.eduhub.auth_service.dto;

import com.eduhub.auth_service.constants.OAuthProvider;
import lombok.Data;

@Data
public class OAuth2UserInfo {
    private String id;
    private String email;
    private String name;
    private OAuthProvider provider;
    public String getProviderName() {
        return provider != null ? provider.name() : null;
    }
}