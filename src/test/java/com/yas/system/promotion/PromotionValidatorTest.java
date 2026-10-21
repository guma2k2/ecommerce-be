package com.yas.system.promotion;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.enumeration.ApplyTo;
import com.yas.system.promotion.internal.enumeration.DiscountType;
import com.yas.system.promotion.internal.enumeration.PromotionUsageStatus;
import com.yas.system.promotion.internal.enumeration.UsageType;
import com.yas.system.promotion.internal.repository.PromotionRepository;
import com.yas.system.promotion.internal.repository.PromotionUsageRepository;
import com.yas.system.promotion.internal.validator.PromotionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionValidatorTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionUsageRepository promotionUsageRepository;

    private PromotionValidator promotionValidator;

    @BeforeEach
    void setUp() {
        promotionValidator = new PromotionValidator(promotionRepository, promotionUsageRepository);
    }

    @Test
    @DisplayName("validateEligibility throws PROMOTION_EXPIRED if current date is outside range")
    void validateEligibility_expired_throwsException() {
        Promotion promotion = Promotion.builder()
                .isActive(true)
                .startDate(ZonedDateTime.now().minusDays(10))
                .endDate(ZonedDateTime.now().minusDays(1))
                .build();

        assertThatThrownBy(() -> promotionValidator.validateEligibility(promotion, BigDecimal.valueOf(100000), "user-1"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.PROMOTION_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("validateEligibility throws PROMOTION_USAGE_EXHAUSTED if usageLimit reached")
    void validateEligibility_usageExhausted_throwsException() {
        Promotion promotion = Promotion.builder()
                .isActive(true)
                .startDate(ZonedDateTime.now().minusDays(1))
                .endDate(ZonedDateTime.now().plusDays(5))
                .usageType(UsageType.LIMITED)
                .usageLimit(10)
                .usageCount(10)
                .build();

        assertThatThrownBy(() -> promotionValidator.validateEligibility(promotion, BigDecimal.valueOf(100000), "user-1"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.PROMOTION_USAGE_EXHAUSTED.getMessage());
    }

    @Test
    @DisplayName("validateEligibility throws MINIMUM_PURCHASE_NOT_MET when subtotal is below minimum")
    void validateEligibility_belowMinimum_throwsException() {
        Promotion promotion = Promotion.builder()
                .isActive(true)
                .startDate(ZonedDateTime.now().minusDays(1))
                .endDate(ZonedDateTime.now().plusDays(5))
                .usageType(UsageType.UNLIMITED)
                .minimumOrderPurchaseAmount(BigDecimal.valueOf(200000.00))
                .build();

        assertThatThrownBy(() -> promotionValidator.validateEligibility(promotion, BigDecimal.valueOf(150000.00), "user-1"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("minimum requirement");
    }

    @Test
    @DisplayName("validateEligibility throws CUSTOMER_USAGE_LIMIT_EXCEEDED when customer reached quota")
    void validateEligibility_customerLimitReached_throwsException() {
        Promotion promotion = Promotion.builder()
                .isActive(true)
                .startDate(ZonedDateTime.now().minusDays(1))
                .endDate(ZonedDateTime.now().plusDays(5))
                .usageType(UsageType.UNLIMITED)
                .usageLimitPerCustomer(1)
                .build();
        promotion.setId(1L);

        when(promotionUsageRepository.countByPromotionIdAndCustomerIdAndStatus(1L, "user-1", PromotionUsageStatus.APPLIED))
                .thenReturn(1L);

        assertThatThrownBy(() -> promotionValidator.validateEligibility(promotion, BigDecimal.valueOf(100000), "user-1"))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.CUSTOMER_USAGE_LIMIT_EXCEEDED.getMessage());
    }
}
