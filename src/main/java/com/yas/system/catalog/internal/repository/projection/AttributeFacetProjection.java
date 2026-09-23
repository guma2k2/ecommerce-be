package com.yas.system.catalog.internal.repository.projection;

public interface AttributeFacetProjection {
    Long getAttributeId();
    String getAttributeName();
    String getAttributeValue();
    Long getProductCount();
}
