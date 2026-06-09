package com.yas.system.auth.internal.redis.service;

import com.yas.system.auth.internal.redis.entity.VerifyEmail;

import java.util.Optional;

public interface VerifyEmailService {
    VerifyEmail saveVerifyEmail(VerifyEmail verifyEmail);
    Optional<VerifyEmail> getByUserAndVerifyCode(String userId, String verifyCode);
    Optional<VerifyEmail> getByVerifyCode(String verifyCode);
    void deleteByUserId(String userId);
}
