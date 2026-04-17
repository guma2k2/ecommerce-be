package com.yas.system.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    @Value("${app.jwt.secret}")
    public String jwtSecret;

    @Value("${app.jwt.access-token-expiration-ms}")
    public long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    public long refreshTokenExpiration;
}
