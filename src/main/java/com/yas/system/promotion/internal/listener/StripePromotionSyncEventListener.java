package com.yas.system.promotion.internal.listener;

import com.yas.system.payment.service.StripePromotionSyncService;
import com.yas.system.payment.api.dto.StripePromotionSyncCommand;
import com.yas.system.payment.api.dto.StripePromotionSyncResult;
import com.yas.system.promotion.events.PromotionSyncEvent;
import com.yas.system.promotion.events.SyncAction;
import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.repository.PromotionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripePromotionSyncEventListener {

    PromotionRepository promotionRepository;
    StripePromotionSyncService stripePromotionSyncService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handlePromotionSync(PromotionSyncEvent event) {
        if (event == null) {
            log.debug("PromotionSyncEvent is null, ignoring.");
            return;
        }

        log.info("Received Stripe PromotionSyncEvent: promotionId={}, action={}",
                event.promotionId(), event.action());

        if (event.action() == SyncAction.DELETE) {
            handleDeleteSync(event);
            return;
        }

        if (event.action() == SyncAction.UPSERT) {
            handleUpsertSync(event);
        }
    }

    private void handleDeleteSync(PromotionSyncEvent event) {
        log.info("Archiving promotion in Stripe: promotionId={}, stripeCouponId={}, stripePromotionCodeId={}",
                event.promotionId(), event.stripeCouponId(), event.stripePromotionCodeId());
        stripePromotionSyncService.archivePromotion(event.stripeCouponId(), event.stripePromotionCodeId());
        log.info("Completed archiving promotion in Stripe: promotionId={}", event.promotionId());
    }

    private void handleUpsertSync(PromotionSyncEvent event) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(event.promotionId());
        if (promotionOpt.isEmpty()) {
            log.warn("Promotion ID {} not found in database for Stripe sync. Skipping.", event.promotionId());
            return;
        }
        Promotion promotion = promotionOpt.get();

        log.info("Synchronizing promotion to Stripe: promotionId={} ('{}')",
                promotion.getId(), promotion.getName());

        StripePromotionSyncCommand command = new StripePromotionSyncCommand(
                promotion.getId(),
                promotion.getName(),
                promotion.getSlug(),
                promotion.getCouponCode(),
                promotion.getDiscountType() != null ? promotion.getDiscountType().name() : DiscountType.PERCENTAGE.name(),
                promotion.getDiscountPercentage(),
                promotion.getDiscountAmount(),
                "usd",
                promotion.getMinimumOrderPurchaseAmount(),
                promotion.getUsageLimit(),
                promotion.getEndDate(),
                promotion.isActive(),
                promotion.getStripeCouponId(),
                promotion.getStripePromotionCodeId()
        );

        StripePromotionSyncResult result = stripePromotionSyncService.syncPromotion(command);

        log.debug("Stripe sync result for promotion ID {}: stripeCouponId='{}', stripePromotionCodeId='{}'",
                promotion.getId(), result.stripeCouponId(), result.stripePromotionCodeId());

        boolean updated = false;
        if (result.stripeCouponId() != null && !Objects.equals(result.stripeCouponId(), promotion.getStripeCouponId())) {
            promotion.setStripeCouponId(result.stripeCouponId());
            updated = true;
        }
        if (result.stripePromotionCodeId() != null && !Objects.equals(result.stripePromotionCodeId(), promotion.getStripePromotionCodeId())) {
            promotion.setStripePromotionCodeId(result.stripePromotionCodeId());
            updated = true;
        }

        if (updated) {
            promotionRepository.save(promotion);
            log.info("Successfully persisted Stripe IDs to DB for promotion ID: {} (coupon={}, promoCode={})",
                    promotion.getId(), promotion.getStripeCouponId(), promotion.getStripePromotionCodeId());
        } else {
            log.debug("No Stripe IDs changed for promotion ID: {}", promotion.getId());
        }
    }
}
