package com.yas.system.promotion;

import com.yas.system.payment.api.StripePromotionPublicService;
import com.yas.system.payment.api.dto.StripePromotionSyncCommand;
import com.yas.system.payment.api.dto.StripePromotionSyncResult;
import com.yas.system.promotion.events.PromotionSyncEvent;
import com.yas.system.promotion.events.SyncAction;
import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.enumeration.UsageType;
import com.yas.system.promotion.internal.listener.StripePromotionSyncEventListener;
import com.yas.system.promotion.internal.repository.PromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripePromotionSyncEventListener Tests")
class StripePromotionSyncEventListenerTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private StripePromotionPublicService stripePromotionPublicService;

    @InjectMocks
    private StripePromotionSyncEventListener listener;

    @Test
    @DisplayName("handlePromotionSync with UPSERT updates and saves promotion with Stripe IDs")
    void handlePromotionSync_upsert_success() {
        Promotion promotion = Promotion.builder()
                .name("Winter Flash Sale")
                .slug("winter-sale")
                .couponCode("WINTER25")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(BigDecimal.valueOf(25.00))
                .usageType(UsageType.LIMITED)
                .usageLimit(200)
                .applyTo(ApplyTo.ORDER)
                .minimumOrderPurchaseAmount(BigDecimal.valueOf(100.00))
                .startDate(ZonedDateTime.now())
                .endDate(ZonedDateTime.now().plusDays(10))
                .isActive(true)
                .build();
        promotion.setId(5L);

        when(promotionRepository.findById(5L)).thenReturn(Optional.of(promotion));
        when(stripePromotionPublicService.syncPromotion(any(StripePromotionSyncCommand.class)))
                .thenReturn(new StripePromotionSyncResult(5L, "cou_winter123", "promo_winter456"));

        PromotionSyncEvent event = new PromotionSyncEvent(5L, SyncAction.UPSERT);
        listener.handlePromotionSync(event);

        assertThat(promotion.getStripeCouponId()).isEqualTo("cou_winter123");
        assertThat(promotion.getStripePromotionCodeId()).isEqualTo("promo_winter456");
        verify(promotionRepository).save(promotion);

        ArgumentCaptor<StripePromotionSyncCommand> captor = ArgumentCaptor.forClass(StripePromotionSyncCommand.class);
        verify(stripePromotionPublicService).syncPromotion(captor.capture());
        assertThat(captor.getValue().couponCode()).isEqualTo("WINTER25");
        assertThat(captor.getValue().discountPercentage()).isEqualByComparingTo(BigDecimal.valueOf(25.00));
    }

    @Test
    @DisplayName("handlePromotionSync with DELETE calls archivePromotion")
    void handlePromotionSync_delete_archivesInStripe() {
        PromotionSyncEvent event = new PromotionSyncEvent(10L, SyncAction.DELETE, "cou_del123", "promo_del456");
        listener.handlePromotionSync(event);

        verify(stripePromotionPublicService).archivePromotion("cou_del123", "promo_del456");
        verify(promotionRepository, never()).findById(any());
        verify(promotionRepository, never()).save(any());
    }

    @Test
    @DisplayName("handlePromotionSync when promotion is not found skips sync")
    void handlePromotionSync_promotionNotFound_skips() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());

        PromotionSyncEvent event = new PromotionSyncEvent(99L, SyncAction.UPSERT);
        listener.handlePromotionSync(event);

        verify(stripePromotionPublicService, never()).syncPromotion(any());
        verify(promotionRepository, never()).save(any());
    }
}
