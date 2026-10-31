package com.yas.system.payment.internal.repository;

import com.yas.system.payment.internal.entity.PaymentLedger;
import com.yas.system.payment.enumeration.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentLedgerRepository extends JpaRepository<PaymentLedger, UUID> {

    Optional<PaymentLedger> findByGatewayProviderAndGatewayTransactionId(PaymentMethod gatewayProvider, String gatewayTransactionId);

    boolean existsByGatewayProviderAndGatewayTransactionId(PaymentMethod gatewayProvider, String gatewayTransactionId);

    List<PaymentLedger> findByPaymentIdOrderByCreatedAtAsc(UUID paymentId);

    List<PaymentLedger> findByOrderIdOrderByCreatedAtAsc(String orderId);
}
