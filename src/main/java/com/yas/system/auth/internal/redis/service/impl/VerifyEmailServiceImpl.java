package com.yas.system.auth.internal.redis.service.impl;

import com.yas.system.auth.internal.redis.entity.VerifyEmail;
import com.yas.system.auth.internal.redis.repository.VerifyEmailRepository;
import com.yas.system.auth.internal.redis.service.VerifyEmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerifyEmailServiceImpl implements VerifyEmailService {

    VerifyEmailRepository verifyEmailRepository;

    @Override
    public VerifyEmail saveVerifyEmail(VerifyEmail verifyEmail) {
        return verifyEmailRepository.save(verifyEmail);
    }

    @Override
    public Optional<VerifyEmail> getByUserAndVerifyCode(String userId, String verifyCode) {
        return verifyEmailRepository.findByUserIdAndVerifyCode(userId, verifyCode);
    }

    @Override
    public Optional<VerifyEmail> getByVerifyCode(String verifyCode) {
        return verifyEmailRepository.findById(verifyCode);
    }

    @Override
    public void deleteByUserId(String userId) {
        verifyEmailRepository.deleteById(userId);
    }
}
