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
@RedisHash("verify_email")
public class VerifyEmail {
    @Id
    @Indexed
    private String verifyCode;

    @Indexed
    private String userId;

    @TimeToLive(unit = TimeUnit.MINUTES)
    private Long timeToLive;
}
