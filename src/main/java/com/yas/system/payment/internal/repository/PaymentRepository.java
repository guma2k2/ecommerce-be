package com.yas.system.payment.internal.repository;

import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByGatewaySessionId(String gatewaySessionId);

    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Optional<Payment> findByIdAndCustomerId(UUID id, String customerId);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findByStatusAndCreatedAtBeforeAndCreatedAtAfter(
            PaymentStatus status,
            ZonedDateTime createdBefore,
            ZonedDateTime createdAfter
    );
}
