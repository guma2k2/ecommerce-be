package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductAttributeRequest;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import org.springframework.stereotype.Component;

@Component
public class ProductAttributeHelper {

    public ProductAttribute createProductAttribute(ProductAttributeRequest request) {
        return ProductAttribute.builder()
                .name(request.name())
                .build();
    }

    public void updateProductAttribute(ProductAttributeRequest request, ProductAttribute productAttribute) {
        productAttribute.setName(request.name());
    }
}
