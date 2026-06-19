package com.yas.system.auth.internal.redis.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@RedisHash("password_reset_token")
public class PasswordResetToken {
    @Id
    private String id;

    private String token;

    private Boolean isUsed;

    @Indexed
    private String userId;

    @TimeToLive(unit = TimeUnit.MINUTES)
    private Long timeToLive;
}
