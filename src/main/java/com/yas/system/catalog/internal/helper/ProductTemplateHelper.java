package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductTemplateCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductTemplateUpdateRequest;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeTemplate;
import com.yas.system.catalog.internal.entity.attribute.ProductTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductTemplateHelper {

    public ProductTemplate createProductTemplate(ProductTemplateCreateRequest request) {
        return ProductTemplate.builder()
                .name(request.name())
                .build();
    }

    public void updateProductTemplate(ProductTemplateUpdateRequest request, ProductTemplate productTemplate) {
        productTemplate.setName(request.name());
    }

    public ProductAttributeTemplate createProductAttributeTemplate(
            ProductTemplate productTemplate,
            ProductAttribute productAttribute,
            int position
    ) {
        return ProductAttributeTemplate.builder()
                .productTemplate(productTemplate)
                .productAttribute(productAttribute)
                .position(position)
                .build();
    }
}
