package com.yas.system.notification.handler;

import com.yas.system.notification.mail.service.MailService;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.event.RefundCompletedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefundCompletedNotificationListener {

    MailService mailService;

    private static final String REFUND_SUBJECT = "Your Refund Has Been Processed";

    @ApplicationModuleListener
    public void handle(RefundCompletedEvent event) {
        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            return;
        }

        boolean isPartial = event.newPaymentStatus() == PaymentStatus.PARTIALLY_REFUNDED;
        BigDecimal remainingAmount = event.totalAmount().subtract(event.refundedAmount());

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("customerName", "Customer");
        templateModel.put("refundedAmount", event.refundedAmount());
        templateModel.put("currency", event.currency());
        templateModel.put("reason", event.reason());
        templateModel.put("gatewayRefundId", "N/A");
        templateModel.put("isPartial", isPartial);
        templateModel.put("remainingAmount", remainingAmount);

        mailService.sendTemplatedEmail(
                event.customerEmail(),
                REFUND_SUBJECT,
                "/templates/refund-completed.html",
                templateModel
        );
    }
}
