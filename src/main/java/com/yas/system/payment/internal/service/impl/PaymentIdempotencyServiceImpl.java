package com.yas.system.payment.internal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.service.PaymentIdempotencyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentIdempotencyServiceImpl implements PaymentIdempotencyService {

    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    private static final Duration LOCK_TTL = Duration.ofMinutes(2);
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);

    @Override
    public Optional<CheckoutSessionResponse> checkAndLock(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return Optional.empty();
        }

        String cacheKey = PaymentConstant.IDEMPOTENCY_CACHE_PREFIX + idempotencyKey;

        try {
            Object existingValue = redisTemplate.opsForValue().get(cacheKey);
            if (existingValue != null) {
                String valStr = existingValue.toString();
                if (PaymentConstant.IDEMPOTENCY_IN_PROGRESS.equalsIgnoreCase(valStr)) {
                    log.warn("Concurrent duplicate payment request detected for idempotency key: {}", idempotencyKey);
                    throw new ApplicationException(ErrorCode.CONCURRENT_PAYMENT_REQUEST);
                }
                return deserializeResponse(valStr);
            }

            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(cacheKey, PaymentConstant.IDEMPOTENCY_IN_PROGRESS, LOCK_TTL);
            if (Boolean.FALSE.equals(acquired)) {
                log.warn("Failed to acquire idempotency lock for key: {}", idempotencyKey);
                throw new ApplicationException(ErrorCode.CONCURRENT_PAYMENT_REQUEST);
            }

            return Optional.empty();
        } catch (ApplicationException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("Redis error while checking idempotency lock for key {}: {}", idempotencyKey, e.getMessage(), e);
            throw new ApplicationException(ErrorCode.PAYMENT_GATEWAY_ERROR, "Idempotency service temporarily unavailable");
        }
    }

    @Override
    public void saveCompleted(String idempotencyKey, CheckoutSessionResponse response) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || response == null) {
            return;
        }

        String cacheKey = PaymentConstant.IDEMPOTENCY_CACHE_PREFIX + idempotencyKey;

        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, COMPLETED_TTL);
        } catch (Exception e) {
            log.error("Failed to save completed payment response to Redis for key {}: {}", idempotencyKey, e.getMessage(), e);
        }
    }

    @Override
    public void unlock(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        String cacheKey = PaymentConstant.IDEMPOTENCY_CACHE_PREFIX + idempotencyKey;
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception e) {
            log.warn("Failed to delete idempotency lock from Redis for key {}: {}", idempotencyKey, e.getMessage());
        }
    }

    private Optional<CheckoutSessionResponse> deserializeResponse(String json) {
        try {
            return Optional.of(objectMapper.readValue(json, CheckoutSessionResponse.class));
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached CheckoutSessionResponse: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
