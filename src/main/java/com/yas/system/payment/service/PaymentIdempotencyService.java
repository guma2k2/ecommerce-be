package com.yas.system.payment.service;

import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;

import java.util.Optional;

public interface PaymentIdempotencyService {

    Optional<CheckoutSessionResponse> checkAndLock(String idempotencyKey);

    void saveCompleted(String idempotencyKey, CheckoutSessionResponse response);

    void unlock(String idempotencyKey);
}

