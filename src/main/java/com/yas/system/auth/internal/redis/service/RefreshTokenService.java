package com.yas.system.auth.internal.redis.service;

import com.yas.system.auth.internal.redis.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshToken saveRefreshToken(RefreshToken refreshToken);
    Optional<RefreshToken> getRefreshTokenByToken(String token);
    void deleteRefreshTokenByToken(String token);
}
