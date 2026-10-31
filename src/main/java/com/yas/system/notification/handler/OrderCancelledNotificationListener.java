package com.yas.system.notification.handler;

import com.yas.system.notification.mail.service.MailService;
import com.yas.system.order.event.OrderCancelledEvent;
import com.yas.system.payment.enumeration.PaymentStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderCancelledNotificationListener {

    MailService mailService;

    private static final String ORDER_CANCELLED_SUBJECT = "Your Order Has Been Cancelled";

    @ApplicationModuleListener
    public void handle(OrderCancelledEvent event) {
        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            return;
        }

        boolean wasRefunded = event.paymentStatus() == PaymentStatus.SUCCEEDED;

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("customerName", "Customer");
        templateModel.put("orderCode", event.orderCode());
        templateModel.put("totalAmount", event.totalAmount());
        templateModel.put("currency", event.currency());
        templateModel.put("wasRefunded", wasRefunded);

        mailService.sendTemplatedEmail(
                event.customerEmail(),
                ORDER_CANCELLED_SUBJECT,
                "/templates/order-cancelled.html",
                templateModel
        );
    }
}
