package com.yas.system.catalog.internal.repository.projection;

import java.math.BigDecimal;

public interface PriceRangeProjection {
    BigDecimal getMinPrice();
    BigDecimal getMaxPrice();
}
