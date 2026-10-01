package com.yas.system.order.internal.repository;

import com.yas.system.order.internal.entity.Order;
import com.yas.system.order.internal.enumeration.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatusOrderByCreatedAtDesc(String customerId, OrderStatus status, Pageable pageable);

    Optional<Order> findByIdAndCustomerId(UUID id, String customerId);

    Optional<Order> findByOrderCode(String orderCode);

    Optional<Order> findByOrderCodeAndCustomerId(String orderCode, String customerId);
}
