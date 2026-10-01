package com.yas.system.payment.internal.repository;

import com.yas.system.payment.internal.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStripeSessionId(String stripeSessionId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    Optional<Payment> findByIdAndCustomerId(Long id, String customerId);
}
