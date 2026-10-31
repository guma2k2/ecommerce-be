package com.yas.system.payment;

import com.yas.system.payment.api.dto.StripePromotionSyncCommand;
import com.yas.system.payment.api.dto.StripePromotionSyncResult;
import com.yas.system.payment.internal.config.StripeProperties;
import com.yas.system.payment.internal.service.impl.StripePromotionSyncServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("StripePromotionSyncService Tests")
class StripePromotionSyncServiceTest {

    @Test
    @DisplayName("syncPromotion returns existing IDs when sync is disabled or apiKey is missing")
    void syncPromotion_disabled_returnsExistingIds() {
        StripeProperties properties = new StripeProperties(
                null,
                "whsec_test",
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                false
        );
        StripePromotionSyncServiceImpl service = new StripePromotionSyncServiceImpl(properties);

        StripePromotionSyncCommand command = new StripePromotionSyncCommand(
                1L,
                "Summer Sale",
                "summer-sale",
                "SUMMER20",
                "PERCENTAGE",
                BigDecimal.valueOf(20.00),
                null,
                "usd",
                BigDecimal.valueOf(50.00),
                100,
                ZonedDateTime.now().plusDays(30),
                true,
                "cou_existing123",
                "promo_existing456"
        );

        StripePromotionSyncResult result = service.syncPromotion(command);

        assertThat(result.promotionId()).isEqualTo(1L);
        assertThat(result.stripeCouponId()).isEqualTo("cou_existing123");
        assertThat(result.stripePromotionCodeId()).isEqualTo("promo_existing456");
    }

    @Test
    @DisplayName("archivePromotion does not throw when sync is disabled")
    void archivePromotion_disabled_doesNotThrow() {
        StripeProperties properties = new StripeProperties(
                null,
                "whsec_test",
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                false
        );
        StripePromotionSyncServiceImpl service = new StripePromotionSyncServiceImpl(properties);

        assertDoesNotThrow(() -> service.archivePromotion("cou_123", "promo_456"));
    }
}

