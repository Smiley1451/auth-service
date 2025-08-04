package com.eduhub.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration")
public class OAuth2Config {
    private Provider google;
    private Provider github;

    @Data
    public static class Provider {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String scope;
    }
}