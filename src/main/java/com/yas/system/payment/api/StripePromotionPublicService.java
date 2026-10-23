package com.yas.system.payment.api;

import com.yas.system.payment.api.dto.StripePromotionSyncCommand;
import com.yas.system.payment.api.dto.StripePromotionSyncResult;

public interface StripePromotionPublicService {

    StripePromotionSyncResult syncPromotion(StripePromotionSyncCommand command);

    void archivePromotion(String stripeCouponId, String stripePromotionCodeId);
}
