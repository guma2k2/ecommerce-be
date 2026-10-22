package com.yas.system.promotion.internal.listener;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.order.event.OrderCancelledEvent;
import com.yas.system.promotion.internal.service.PromotionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionOrderEventListener {

    PromotionService promotionService;

    @Async
    @EventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelledEvent for orderId: {}, reverting promotion usage if applicable", event.orderId());
        try {
            promotionService.revertPromotionUsage(event.orderId());
        } catch (ApplicationException e) {
            log.error("Failed to revert promotion usage for orderId: {}", event.orderId(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while reverting promotion usage for orderId: {}", event.orderId(), e);
            throw new ApplicationException(ErrorCode.UNCATEGORIZED);
        }
    }
}
