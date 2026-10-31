package com.yas.system.payment.internal.service.impl;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Coupon;
import com.stripe.model.PromotionCode;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.CouponUpdateParams;
import com.stripe.param.PromotionCodeCreateParams;
import com.stripe.param.PromotionCodeUpdateParams;
import com.yas.system.payment.service.StripePromotionSyncService;
import com.yas.system.payment.api.dto.StripePromotionSyncCommand;
import com.yas.system.payment.api.dto.StripePromotionSyncResult;
import com.yas.system.payment.internal.config.StripeProperties;
import com.yas.system.payment.internal.constant.PaymentConstant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripePromotionSyncServiceImpl implements StripePromotionSyncService {

    StripeProperties stripeProperties;

    @Override
    public StripePromotionSyncResult syncPromotion(StripePromotionSyncCommand command) {
        if (!stripeProperties.isSyncEnabled()) {
            log.debug("Stripe promotion sync is disabled. Skipping promotion ID: {}", command.promotionId());
            return new StripePromotionSyncResult(
                    command.promotionId(),
                    command.existingStripeCouponId(),
                    command.existingStripePromotionCodeId()
            );
        }

        String currency = (command.currency() != null && !command.currency().isBlank())
                ? command.currency().toLowerCase()
                : PaymentConstant.DEFAULT_CURRENCY;

        log.debug("Starting Stripe sync for promotion ID: {} (couponCode='{}', type='{}', active={})",
                command.promotionId(), command.couponCode(), command.discountType(), command.active());

        StripeClient client = new StripeClient(stripeProperties.apiKey());

        try {
            if (command.existingStripeCouponId() == null || command.existingStripeCouponId().isBlank()) {
                return createStripeCouponAndPromotionCode(client, command, currency);
            } else {
                return updateStripeCouponAndPromotionCode(client, command, currency);
            }
        } catch (StripeException e) {
            log.error("Failed to sync promotion {} with Stripe: {}", command.promotionId(), e.getMessage(), e);
            return new StripePromotionSyncResult(
                    command.promotionId(),
                    command.existingStripeCouponId(),
                    command.existingStripePromotionCodeId()
            );
        }
    }

    private StripePromotionSyncResult createStripeCouponAndPromotionCode(
            StripeClient client,
            StripePromotionSyncCommand command,
            String currency
    ) throws StripeException {
        log.debug("Creating Stripe Coupon and PromotionCode for promotion ID: {}", command.promotionId());

        CouponCreateParams.Builder couponBuilder = CouponCreateParams.builder()
                .setDuration(CouponCreateParams.Duration.ONCE)
                .setName(command.name())
                .putMetadata(PaymentConstant.MetadataKey.PROMOTION_ID, String.valueOf(command.promotionId()))
                .putMetadata(PaymentConstant.MetadataKey.COUPON_CODE, command.couponCode())
                .putMetadata(PaymentConstant.MetadataKey.SLUG, command.slug() != null ? command.slug() : "");

        if ("PERCENTAGE".equalsIgnoreCase(command.discountType())) {
            couponBuilder.setPercentOff(command.discountPercentage());
        } else {
            long amountOffInCents = command.discountAmount() != null
                    ? command.discountAmount().multiply(PaymentConstant.CENTS_MULTIPLIER).longValue()
                    : 0L;
            couponBuilder.setAmountOff(amountOffInCents);
            couponBuilder.setCurrency(currency);
        }

        if (command.usageLimit() != null && command.usageLimit() > 0) {
            couponBuilder.setMaxRedemptions(command.usageLimit().longValue());
        }

        if (command.endDate() != null) {
            couponBuilder.setRedeemBy(command.endDate().toEpochSecond());
        }

        Coupon stripeCoupon = client.coupons().create(couponBuilder.build());
        log.info("Successfully created Stripe Coupon {} for promotion ID: {}", stripeCoupon.getId(), command.promotionId());

        PromotionCodeCreateParams.Builder promoBuilder = PromotionCodeCreateParams.builder()
                .setCoupon(stripeCoupon.getId())
                .setCode(command.couponCode())
                .setActive(command.active())
                .putMetadata(PaymentConstant.MetadataKey.PROMOTION_ID, String.valueOf(command.promotionId()));

        if (command.usageLimit() != null && command.usageLimit() > 0) {
            promoBuilder.setMaxRedemptions(command.usageLimit().longValue());
        }

        if (command.endDate() != null) {
            promoBuilder.setExpiresAt(command.endDate().toEpochSecond());
        }

        if (command.minimumOrderPurchaseAmount() != null && command.minimumOrderPurchaseAmount().compareTo(BigDecimal.ZERO) > 0) {
            long minAmountInCents = command.minimumOrderPurchaseAmount().multiply(PaymentConstant.CENTS_MULTIPLIER).longValue();
            PromotionCodeCreateParams.Restrictions restrictions = PromotionCodeCreateParams.Restrictions.builder()
                    .setMinimumAmount(minAmountInCents)
                    .setMinimumAmountCurrency(currency)
                    .build();
            promoBuilder.setRestrictions(restrictions);
        }

        PromotionCode stripePromoCode = client.promotionCodes().create(promoBuilder.build());
        log.info("Successfully created Stripe PromotionCode {} (code='{}') for promotion ID: {}",
                stripePromoCode.getId(), stripePromoCode.getCode(), command.promotionId());

        return new StripePromotionSyncResult(command.promotionId(), stripeCoupon.getId(), stripePromoCode.getId());
    }

    private StripePromotionSyncResult updateStripeCouponAndPromotionCode(
            StripeClient client,
            StripePromotionSyncCommand command,
            String currency
    ) throws StripeException {
        String existingCouponId = command.existingStripeCouponId();
        String existingPromoCodeId = command.existingStripePromotionCodeId();

        boolean discountValuesChanged = false;
        try {
            Coupon existingCoupon = client.coupons().retrieve(existingCouponId);
            if (Boolean.TRUE.equals(existingCoupon.getDeleted())) {
                discountValuesChanged = true;
            } else if ("PERCENTAGE".equalsIgnoreCase(command.discountType())) {
                discountValuesChanged = existingCoupon.getPercentOff() == null
                        || existingCoupon.getPercentOff().compareTo(command.discountPercentage()) != 0;
            } else {
                long requestedAmountOff = command.discountAmount() != null
                        ? command.discountAmount().multiply(PaymentConstant.CENTS_MULTIPLIER).longValue()
                        : 0L;
                discountValuesChanged = existingCoupon.getAmountOff() == null
                        || existingCoupon.getAmountOff() != requestedAmountOff
                        || !currency.equalsIgnoreCase(existingCoupon.getCurrency());
            }
        } catch (StripeException e) {
            log.warn("Could not retrieve existing Stripe Coupon {}: {}. Will recreate.", existingCouponId, e.getMessage());
            discountValuesChanged = true;
        }

        if (discountValuesChanged) {
            log.info("Discount parameters changed for promotion ID: {}. Recreating Stripe Coupon and PromotionCode.", command.promotionId());
            archivePromotion(existingCouponId, existingPromoCodeId);
            return createStripeCouponAndPromotionCode(client, command, currency);
        }

        CouponUpdateParams couponUpdateParams = CouponUpdateParams.builder()
                .setName(command.name())
                .putMetadata(PaymentConstant.MetadataKey.COUPON_CODE, command.couponCode())
                .putMetadata(PaymentConstant.MetadataKey.SLUG, command.slug() != null ? command.slug() : "")
                .build();
        client.coupons().update(existingCouponId, couponUpdateParams);

        if (existingPromoCodeId != null && !existingPromoCodeId.isBlank()) {
            try {
                PromotionCode existingPromo = client.promotionCodes().retrieve(existingPromoCodeId);
                if (!Objects.equals(existingPromo.getActive(), command.active())) {
                    PromotionCodeUpdateParams promoUpdateParams = PromotionCodeUpdateParams.builder()
                            .setActive(command.active())
                            .build();
                    client.promotionCodes().update(existingPromoCodeId, promoUpdateParams);
                    log.info("Updated Stripe PromotionCode {} active status to {} for promotion ID: {}",
                            existingPromoCodeId, command.active(), command.promotionId());
                }
            } catch (StripeException e) {
                log.warn("Failed to update Stripe PromotionCode {}: {}", existingPromoCodeId, e.getMessage());
            }
        }

        return new StripePromotionSyncResult(command.promotionId(), existingCouponId, existingPromoCodeId);
    }

    @Override
    public void archivePromotion(String stripeCouponId, String stripePromotionCodeId) {
        if (!stripeProperties.isSyncEnabled()) {
            log.debug("Stripe sync disabled. Skipping archive for coupon={}, promoCode={}", stripeCouponId, stripePromotionCodeId);
            return;
        }

        log.info("Archiving Stripe coupon: '{}', promoCode: '{}'", stripeCouponId, stripePromotionCodeId);
        StripeClient client = new StripeClient(stripeProperties.apiKey());

        if (stripePromotionCodeId != null && !stripePromotionCodeId.isBlank()) {
            try {
                client.promotionCodes().update(
                        stripePromotionCodeId,
                        PromotionCodeUpdateParams.builder().setActive(false).build()
                );
                log.info("Deactivated Stripe promotion code: {}", stripePromotionCodeId);
            } catch (StripeException e) {
                log.warn("Failed to deactivate Stripe promotion code {}: {}", stripePromotionCodeId, e.getMessage());
            }
        }

        if (stripeCouponId != null && !stripeCouponId.isBlank()) {
            try {
                client.coupons().delete(stripeCouponId);
                log.info("Deleted Stripe coupon: {}", stripeCouponId);
            } catch (StripeException e) {
                log.warn("Failed to delete Stripe coupon {}: {}", stripeCouponId, e.getMessage());
            }
        }
    }
}

