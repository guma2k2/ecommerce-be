package com.yas.system.payment.internal.service.impl;

import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.enumeration.BalanceType;
import com.yas.system.payment.enumeration.PaymentStatus;
import com.yas.system.payment.event.PaymentCompletedEvent;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.gateway.PaymentGatewayResolver;
import com.yas.system.payment.internal.gateway.dto.PaymentStatusResult;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.service.PaymentLedgerService;
import com.yas.system.payment.service.PaymentReconciliationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentReconciliationServiceImpl implements PaymentReconciliationService {

    PaymentRepository paymentRepository;
    PaymentGatewayResolver paymentGatewayResolver;
    PaymentLedgerService paymentLedgerService;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void reconcilePendingPayments() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime createdBefore = now.minus(Duration.ofMinutes(PaymentConstant.RECONCILIATION_STALE_MINUTES));
        ZonedDateTime createdAfter = now.minus(Duration.ofHours(PaymentConstant.RECONCILIATION_WINDOW_HOURS));

        List<Payment> stalePayments = paymentRepository.findByStatusAndCreatedAtBeforeAndCreatedAtAfter(
                PaymentStatus.PENDING,
                createdBefore,
                createdAfter
        );

        if (stalePayments.isEmpty()) {
            log.debug("No stale pending payments found for reconciliation");
            return;
        }

        log.info("Found {} stale pending payments to reconcile", stalePayments.size());

        for (Payment payment : stalePayments) {
            try {
                reconcileSinglePayment(payment);
            } catch (Exception e) {
                log.error("Failed to reconcile payment with ID {}: {}", payment.getId(), e.getMessage(), e);
            }
        }
    }

    private void reconcileSinglePayment(Payment payment) {
        PaymentGateway gateway = paymentGatewayResolver.resolve(payment.getMethod());
        PaymentStatusResult statusResult = gateway.queryTransactionStatus(payment);

        log.info("Reconciliation status for payment {}: gatewayStatus={}, rawStatus={}",
                payment.getId(), statusResult.status(), statusResult.rawStatus());

        if (statusResult.status() == PaymentStatus.SUCCEEDED) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            if (statusResult.gatewayTransactionId() != null) {
                payment.setGatewayTransactionId(statusResult.gatewayTransactionId());
            }
            paymentRepository.save(payment);

            paymentLedgerService.recordCapture(
                    payment,
                    payment.getGatewayTransactionId(),
                    "Reconciliation synced SUCCEEDED state from gateway"
            );

            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    payment.getId(),
                    payment.getCustomerId(),
                    payment.getOrderId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getGatewayTransactionId()
            ));

            log.info("Reconciliation resolved payment {} to SUCCEEDED and triggered completion event", payment.getId());
        } else if (statusResult.status() == PaymentStatus.FAILED || statusResult.status() == PaymentStatus.CANCELLED) {
            payment.setStatus(statusResult.status());
            if (statusResult.failureReason() != null) {
                payment.setFailureReason(statusResult.failureReason());
            }
            paymentRepository.save(payment);

            paymentLedgerService.recordAdjustment(
                    payment,
                    payment.getAmount(),
                    BalanceType.CREDIT,
                    "Reconciliation resolved payment to " + statusResult.status() + ": " + statusResult.failureReason()
            );

            log.info("Reconciliation resolved payment {} to {}", payment.getId(), statusResult.status());
        }
    }
}
