package com.yas.system.promotion.events;

public record PromotionSyncEvent(
        Long promotionId,
        SyncAction action,
        String stripeCouponId,
        String stripePromotionCodeId
) {
    public PromotionSyncEvent(Long promotionId, SyncAction action) {
        this(promotionId, action, null, null);
    }
}
