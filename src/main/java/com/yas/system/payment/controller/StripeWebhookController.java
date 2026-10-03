package com.yas.system.payment.controller;

import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.internal.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripeWebhookController {

    PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = PaymentConstant.STRIPE_SIGNATURE_HEADER, required = false) String signatureHeader
    ) {
        log.info("Received Stripe webhook request (payload size: {} bytes, hasSignature: {})",
                payload != null ? payload.length() : 0, signatureHeader != null);
        paymentService.handleWebhookEvent(payload, signatureHeader);
        return ResponseEntity.ok().build();
    }
}
