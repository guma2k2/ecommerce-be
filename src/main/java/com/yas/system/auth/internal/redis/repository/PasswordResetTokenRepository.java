package com.yas.system.auth.internal.redis.repository;

import com.yas.system.auth.internal.redis.entity.PasswordResetToken;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends ListCrudRepository<PasswordResetToken, String> {
}
