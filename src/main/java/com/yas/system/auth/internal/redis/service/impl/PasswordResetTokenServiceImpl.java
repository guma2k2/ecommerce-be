package com.yas.system.auth.internal.redis.service.impl;

import com.yas.system.auth.internal.redis.entity.PasswordResetToken;
import com.yas.system.auth.internal.redis.repository.PasswordResetTokenRepository;
import com.yas.system.auth.internal.redis.service.PasswordResetTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public PasswordResetToken save(PasswordResetToken resetPassword) {
        return passwordResetTokenRepository.save(resetPassword);
    }

    @Override
    public Optional<PasswordResetToken> getResetPasswordByCode(String code) {
        return passwordResetTokenRepository.findById(code);
    }

    @Override
    public void delete(String code) {
        passwordResetTokenRepository.deleteById(code);
    }
}
