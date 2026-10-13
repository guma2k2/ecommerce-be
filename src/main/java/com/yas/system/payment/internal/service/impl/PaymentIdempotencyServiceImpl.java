package com.yas.system.payment.internal.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.service.PaymentIdempotencyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentIdempotencyServiceImpl implements PaymentIdempotencyService {

    RedisTemplate<String, Object> redisTemplate;
    ObjectMapper objectMapper;

    // In-memory fallback if Redis is unavailable or in test environments
    Map<String, String> localFallbackMap = new ConcurrentHashMap<>();

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
            log.warn("Redis idempotency check encountered error (falling back to memory): {}", e.getMessage());
            return checkFallback(cacheKey, idempotencyKey);
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
            localFallbackMap.put(cacheKey, json);
        } catch (Exception e) {
            log.warn("Failed to save completed payment response to Redis for key {}: {}", idempotencyKey, e.getMessage());
            try {
                localFallbackMap.put(cacheKey, objectMapper.writeValueAsString(response));
            } catch (Exception ignored) {
            }
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
        } catch (Exception ignored) {
        }
        localFallbackMap.remove(cacheKey);
    }

    private Optional<CheckoutSessionResponse> checkFallback(String cacheKey, String idempotencyKey) {
        String existing = localFallbackMap.get(cacheKey);
        if (existing != null) {
            if (PaymentConstant.IDEMPOTENCY_IN_PROGRESS.equalsIgnoreCase(existing)) {
                throw new ApplicationException(ErrorCode.CONCURRENT_PAYMENT_REQUEST);
            }
            return deserializeResponse(existing);
        }
        localFallbackMap.put(cacheKey, PaymentConstant.IDEMPOTENCY_IN_PROGRESS);
        return Optional.empty();
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
