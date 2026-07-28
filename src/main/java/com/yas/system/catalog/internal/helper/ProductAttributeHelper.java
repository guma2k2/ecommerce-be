package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductAttributeCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductAttributeUpdateRequest;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import org.springframework.stereotype.Component;

@Component
public class ProductAttributeHelper {

    public ProductAttribute createProductAttribute(ProductAttributeCreateRequest request) {
        return ProductAttribute.builder()
                .name(request.name())
                .build();
    }

    public void updateProductAttribute(ProductAttributeUpdateRequest request, ProductAttribute productAttribute) {
        productAttribute.setName(request.name());
    }
}
