package com.yas.system.auth.internal.redis.service;

import com.yas.system.auth.internal.redis.entity.RefreshToken;
import com.yas.system.auth.internal.redis.entity.VerifyEmail;

import java.util.Optional;

public interface VerifyEmailService {
    VerifyEmail saveVerifyEmail(VerifyEmail verifyEmail);
    Optional<VerifyEmail> getByUserAndVerifyCode(String userId, String verifyCode);
    void deleteByUserId(String userId);
}
