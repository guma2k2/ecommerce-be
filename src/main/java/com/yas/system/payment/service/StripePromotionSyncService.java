package com.yas.system.payment.service;

import com.yas.system.payment.api.dto.StripePromotionSyncCommand;
import com.yas.system.payment.api.dto.StripePromotionSyncResult;

public interface StripePromotionSyncService {

    StripePromotionSyncResult syncPromotion(StripePromotionSyncCommand command);

    void archivePromotion(String stripeCouponId, String stripePromotionCodeId);
}

