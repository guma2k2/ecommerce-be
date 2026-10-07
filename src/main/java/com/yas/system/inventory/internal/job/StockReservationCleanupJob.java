package com.yas.system.inventory.internal.job;

import com.yas.system.inventory.internal.service.StockReservationService;
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
public class StockReservationCleanupJob {

    StockReservationService stockReservationService;

    @Scheduled(cron = "0 */5 * * * *")
    public void cleanupExpiredReservations() {
        log.info("Running scheduled expired stock reservations cleanup");
        try {
            stockReservationService.releaseExpiredReservations();
        } catch (Exception e) {
            log.error("Failed to release expired stock reservations", e);
        }
    }
}
