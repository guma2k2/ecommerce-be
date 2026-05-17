package com.yas.system.auth.internal.redis.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@RedisHash("verify_email")
public class VerifyEmail {
    @Id
    @Indexed
    private String userId;

    @Indexed
    private String verifyCode;

    @TimeToLive(unit = TimeUnit.MINUTES)
    private Long timeToLive;
}
