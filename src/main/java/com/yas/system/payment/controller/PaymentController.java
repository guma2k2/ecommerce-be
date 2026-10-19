package com.yas.system.payment.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.annotation.ActiveUser;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.internal.dto.request.CreateCheckoutSessionRequest;
import com.yas.system.payment.internal.dto.request.RefundPaymentRequest;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.dto.response.PaymentLedgerResponse;
import com.yas.system.payment.internal.dto.response.PaymentResponse;
import com.yas.system.payment.internal.dto.response.RefundPaymentResponse;
import com.yas.system.payment.internal.gateway.dto.RefundResult;
import com.yas.system.payment.internal.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/checkout-session")
    public ApiResponse<CheckoutSessionResponse> createCheckoutSession(
            @ActiveUser AuthUser customer,
            @RequestHeader(value = PaymentConstant.IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CreateCheckoutSessionRequest request
    ) {
        return ApiResponse.success(paymentService.createCheckoutSession(customer, idempotencyKey, request));
    }

    @PostMapping("/{paymentId}/refund")
    public ApiResponse<RefundPaymentResponse> refundPayment(
            @ActiveUser AuthUser customer,
            @PathVariable UUID paymentId,
            @RequestHeader(value = PaymentConstant.IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody RefundPaymentRequest request
    ) {
        RefundResult result = paymentService.refundPayment(customer, paymentId, request, idempotencyKey);
        PaymentResponse updated = paymentService.getPaymentById(customer, paymentId);
        return ApiResponse.success(RefundPaymentResponse.from(
                paymentId,
                updated.orderId(),
                result,
                updated.refundedAmount(),
                updated.amount().subtract(updated.refundedAmount()),
                updated.status()
        ));
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getPayment(
            @ActiveUser AuthUser customer,
            @PathVariable UUID paymentId
    ) {
        return ApiResponse.success(paymentService.getPaymentById(customer, paymentId));
    }

    @GetMapping("/{paymentId}/ledger")
    public ApiResponse<List<PaymentLedgerResponse>> getPaymentLedger(
            @ActiveUser AuthUser customer,
            @PathVariable UUID paymentId
    ) {
        return ApiResponse.success(paymentService.getPaymentLedger(customer, paymentId));
    }

    @GetMapping
    public ApiResponse<List<PaymentResponse>> getCustomerPayments(@ActiveUser AuthUser customer) {
        return ApiResponse.success(paymentService.getCustomerPayments(customer));
    }
}
