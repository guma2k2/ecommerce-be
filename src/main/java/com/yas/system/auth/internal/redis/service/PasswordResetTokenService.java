package com.yas.system.auth.internal.redis.service;

import com.yas.system.auth.internal.redis.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenService {
    PasswordResetToken save(PasswordResetToken resetPassword);
    Optional<PasswordResetToken> getResetPasswordByCode(String code);
    void delete(String code);
}
