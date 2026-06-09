package com.yas.system.auth.internal.redis.repository;

import com.yas.system.auth.internal.redis.entity.VerifyEmail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerifyEmailRepository extends ListCrudRepository<VerifyEmail, String> {

    Optional<VerifyEmail> findByUserIdAndVerifyCode(String userId, String verifyCode);
}
