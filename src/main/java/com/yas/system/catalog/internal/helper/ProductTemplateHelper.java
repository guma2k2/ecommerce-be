package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductTemplateRequest;
import com.yas.system.catalog.internal.entity.attribute.ProductTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductTemplateHelper {

    public ProductTemplate createProductTemplate(ProductTemplateRequest request) {
        return ProductTemplate.builder()
                .name(request.name())
                .build();
    }

    public void updateProductTemplate(ProductTemplateRequest request, ProductTemplate productTemplate) {
        productTemplate.setName(request.name());
    }
}
