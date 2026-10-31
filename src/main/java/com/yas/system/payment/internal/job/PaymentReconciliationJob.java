package com.yas.system.payment.internal.job;

import com.yas.system.payment.service.PaymentReconciliationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentReconciliationJob {

    PaymentReconciliationService paymentReconciliationService;

    @Scheduled(cron = "${app.payment.reconciliation.cron:0 */10 * * * *}")
    public void runReconciliation() {
        log.info("Starting scheduled payment reconciliation job");
        try {
            paymentReconciliationService.reconcilePendingPayments();
        } catch (Exception e) {
            log.error("Error occurred during scheduled payment reconciliation", e);
        }
    }
}
