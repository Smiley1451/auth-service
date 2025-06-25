package com.eduhub.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Config {
    private Google google;
    private Github github;

    @Data
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }

    @Data
    public static class Github {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
}