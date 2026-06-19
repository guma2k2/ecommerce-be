package com.yas.system.auth.internal.redis.service.impl;

import com.yas.system.auth.internal.redis.entity.PasswordResetToken;
import com.yas.system.auth.internal.redis.service.PasswordResetTokenService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {
    @Override
    public PasswordResetToken save(PasswordResetToken resetPassword) {
        return null;
    }

    @Override
    public Optional<PasswordResetToken> getResetPasswordByCode(String code) {
        return Optional.empty();
    }

    @Override
    public void delete(String code) {

    }
}
