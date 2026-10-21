package com.yas.system.promotion;

import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.promotion.internal.dto.request.VerifyPromotionItemRequest;
import com.yas.system.promotion.internal.dto.response.PromotionVerifyResponse;
import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.entity.PromotionApply;
import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.helper.PromotionHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromotionHelperTest {

    private PromotionHelper promotionHelper;

    @BeforeEach
    void setUp() {
        promotionHelper = new PromotionHelper();
    }

    @Test
    @DisplayName("calculateDiscount for whole ORDER with PERCENTAGE discount")
    void calculateDiscount_orderPercentage_success() {
        Promotion promotion = Promotion.builder()
                .name("Order 10% off")
                .couponCode("SAVE10")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(BigDecimal.valueOf(10.00))
                .applyTo(ApplyTo.ORDER)
                .build();
        promotion.setId(1L);

        BigDecimal orderAmount = BigDecimal.valueOf(500000.00);
        List<VerifyPromotionItemRequest> items = List.of(
                new VerifyPromotionItemRequest(101L, 2, BigDecimal.valueOf(250000.00))
        );

        PromotionVerifyResponse response = promotionHelper.calculateDiscount(promotion, orderAmount, items, Map.of());

        assertThat(response.isValid()).isTrue();
        assertThat(response.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(50000.00));
        assertThat(response.couponCode()).isEqualTo("SAVE10");
    }

    @Test
    @DisplayName("calculateDiscount with PERCENTAGE discount capped by maxDiscountAmount")
    void calculateDiscount_percentageCapped_success() {
        Promotion promotion = Promotion.builder()
                .name("50% off max 100k")
                .couponCode("BIG50")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(BigDecimal.valueOf(50.00))
                .maxDiscountAmount(BigDecimal.valueOf(100000.00))
                .applyTo(ApplyTo.ORDER)
                .build();
        promotion.setId(2L);

        BigDecimal orderAmount = BigDecimal.valueOf(1000000.00);
        List<VerifyPromotionItemRequest> items = List.of(
                new VerifyPromotionItemRequest(101L, 1, BigDecimal.valueOf(1000000.00))
        );

        PromotionVerifyResponse response = promotionHelper.calculateDiscount(promotion, orderAmount, items, Map.of());

        assertThat(response.isValid()).isTrue();
        assertThat(response.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000.00));
    }

    @Test
    @DisplayName("calculateDiscount for CATEGORY target should apply only to eligible items")
    void calculateDiscount_categoryTarget_success() {
        Promotion promotion = Promotion.builder()
                .name("Shoes category 20% off")
                .couponCode("SHOES20")
                .discountType(DiscountType.PERCENTAGE)
                .discountPercentage(BigDecimal.valueOf(20.00))
                .applyTo(ApplyTo.CATEGORY)
                .applies(new ArrayList<>())
                .build();
        promotion.setId(3L);
        promotion.getApplies().add(PromotionApply.builder().promotion(promotion).categoryId(10L).build());

        // Variant 101 belongs to category 10 (eligible: 200,000 * 1 = 200,000)
        ProductVariantPublicDto variant1 = new ProductVariantPublicDto(
                101L, 1L, "Running Shoes", "running-shoes", 10L, 5L, null, 10, "SKU1",
                BigDecimal.valueOf(200000.00), List.of(), null, null
        );
        // Variant 102 belongs to category 99 (not eligible)
        ProductVariantPublicDto variant2 = new ProductVariantPublicDto(
                102L, 2L, "T-Shirt", "t-shirt", 99L, 5L, null, 10, "SKU2",
                BigDecimal.valueOf(300000.00), List.of(), null, null
        );

        List<VerifyPromotionItemRequest> items = List.of(
                new VerifyPromotionItemRequest(101L, 1, BigDecimal.valueOf(200000.00)),
                new VerifyPromotionItemRequest(102L, 1, BigDecimal.valueOf(300000.00))
        );

        PromotionVerifyResponse response = promotionHelper.calculateDiscount(
                promotion,
                BigDecimal.valueOf(500000.00),
                items,
                Map.of(101L, variant1, 102L, variant2)
        );

        assertThat(response.isValid()).isTrue();
        // 20% of 200,000 = 40,000
        assertThat(response.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(40000.00));
        assertThat(response.eligibleVariantIds()).containsExactly(101L);
    }

    @Test
    @DisplayName("calculateDiscount should throw PROMOTION_NOT_APPLICABLE when no items match category")
    void calculateDiscount_noMatchingItems_throwsException() {
        Promotion promotion = Promotion.builder()
                .name("Electronics only")
                .couponCode("ELEC15")
                .discountType(DiscountType.FIXED)
                .discountAmount(BigDecimal.valueOf(50000.00))
                .applyTo(ApplyTo.CATEGORY)
                .applies(new ArrayList<>())
                .build();
        promotion.setId(4L);
        promotion.getApplies().add(PromotionApply.builder().promotion(promotion).categoryId(999L).build());

        ProductVariantPublicDto variant = new ProductVariantPublicDto(
                101L, 1L, "Book", "book", 10L, 5L, null, 10, "SKU1",
                BigDecimal.valueOf(100000.00), List.of(), null, null
        );

        List<VerifyPromotionItemRequest> items = List.of(
                new VerifyPromotionItemRequest(101L, 1, BigDecimal.valueOf(100000.00))
        );

        assertThatThrownBy(() -> promotionHelper.calculateDiscount(
                promotion,
                BigDecimal.valueOf(100000.00),
                items,
                Map.of(101L, variant)
        ))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.PROMOTION_NOT_APPLICABLE.getMessage());
    }
}
