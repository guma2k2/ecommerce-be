package com.yas.system.catalog.internal.repository.projection;

import java.math.BigDecimal;

public interface ProductSuggestionProjection {
    Long getId();
    String getName();
    String getSlug();
    String getCategoryName();
    String getMediaId();
    BigDecimal getMinPrice();
}
