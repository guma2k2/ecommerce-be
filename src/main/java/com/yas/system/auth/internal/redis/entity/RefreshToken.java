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
@RedisHash("refresh_token")
public class RefreshToken {
    @Id
    private String id;

    @Indexed
    private String token;

    @TimeToLive(unit = TimeUnit.DAYS)
    private long expiresAt;

}
