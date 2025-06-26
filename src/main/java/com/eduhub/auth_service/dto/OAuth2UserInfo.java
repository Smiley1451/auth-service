package com.eduhub.auth_service.dto;

import com.eduhub.auth_service.constants.OAuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
    private String id;
    private String email;
    private String name;
    private OAuthProvider provider;
    private static final long serialVersionUID = 1L;


    public static OAuth2UserInfo fromOAuth2User(OAuth2User oauth2User) {
        OAuth2UserInfo userInfo = new OAuth2UserInfo();
        String providerName = oauth2User.getAttribute("provider") != null
                ? oauth2User.getAttribute("provider")
                : determineProvider(oauth2User);

        switch (providerName.toLowerCase()) {
            case "google":
                userInfo.setProvider(OAuthProvider.GOOGLE);
                userInfo.setId(oauth2User.getAttribute("sub")); // Google uses 'sub' for user ID
                userInfo.setEmail(oauth2User.getAttribute("email"));
                break;
            case "github":
                userInfo.setProvider(OAuthProvider.GITHUB);
                userInfo.setId(String.valueOf(oauth2User.getAttribute("id"))); // GitHub uses 'id' as integer
                userInfo.setEmail(oauth2User.getAttribute("email")); // May require additional API call if null
                break;
            default:
                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + providerName);
        }

        if (userInfo.getId() == null || userInfo.getEmail() == null) {
            throw new IllegalArgumentException("Missing required OAuth2 attributes: id or email");
        }

        return userInfo;
    }

    /**
     * Determines the OAuth2 provider based on OAuth2User attributes or authorities.
     *
     * @param oauth2User The OAuth2User object.
     * @return The provider name (e.g., "google", "github").
     */
    private static String determineProvider(OAuth2User oauth2User) {
        // Fallback logic to determine provider if not explicitly set
        if (oauth2User.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().contains("google"))) {
            return "google";
        } else if (oauth2User.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().contains("github"))) {
            return "github";
        }
        return "unknown";
    }

    public String getProviderName() {
        return provider != null ? provider.name() : null;
    }
}