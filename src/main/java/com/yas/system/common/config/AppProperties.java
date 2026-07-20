package com.yas.system.common.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String name,
        Jwt jwt,
        OAuth2 oauth2,
        String frontendErrorUrl,
        ClientUrl clientUrl
) {

    public record ClientUrl(
      String backoffice,
      String storefront
    ){}

    public record Jwt(
            String secret,
            long accessTokenExpirationMs,
            long refreshTokenExpirationMs
    ) {
    }

    public record OAuth2(
            Provider google,
            Provider facebook,
            Provider github
    ) {
    }

    public record Provider(
            String clientId,
            String clientSecret,
            String redirectUri,
            List<String> scope
    ) {
    }
}