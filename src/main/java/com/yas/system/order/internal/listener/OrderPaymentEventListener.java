package com.yas.system.order.internal.listener;

import com.yas.system.inventory.service.InventoryService;
import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.order.internal.repository.OrderRepository;
import com.yas.system.payment.enumeration.PaymentStatus;
import com.yas.system.payment.event.PaymentCompletedEvent;
import com.yas.system.payment.event.RefundCompletedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderPaymentEventListener {

    OrderRepository orderRepository;
    InventoryService inventoryService;

    @EventListener
    @Transactional
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        if (event == null || event.orderId() == null) {
            return;
        }

        try {
            UUID orderId = UUID.fromString(event.orderId());
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setPaymentStatus(PaymentStatus.SUCCEEDED);
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.CONFIRMED);
                }
                orderRepository.save(order);
                inventoryService.confirmDeductions(event.orderId());
                log.info("Order {} successfully updated to SUCCEEDED and CONFIRMED via PaymentCompletedEvent", orderId);
            });
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order UUID in PaymentCompletedEvent: {}", event.orderId());
        }
    }

    @EventListener
    @Async
    @Transactional
    public void onRefundCompleted(RefundCompletedEvent event) {
        if (event == null || event.orderId() == null) {
            return;
        }

        try {
            UUID orderId = UUID.fromString(event.orderId());
            orderRepository.findById(orderId).ifPresent(order -> {
                order.setPaymentStatus(event.newPaymentStatus());
                orderRepository.save(order);
                log.info("Order {} payment status updated to {} via RefundCompletedEvent",
                        orderId, event.newPaymentStatus());
            });
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order UUID in RefundCompletedEvent: {}", event.orderId());
        }
    }
}
