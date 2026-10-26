package com.yas.system.promotion.internal.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PromotionConstant {

    public static final BigDecimal PERCENTAGE_DIVISOR = BigDecimal.valueOf(100);
    public static final BigDecimal MAX_PERCENTAGE = BigDecimal.valueOf(100);
    public static final int CURRENCY_SCALE = 2;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
}
