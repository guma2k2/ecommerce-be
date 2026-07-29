package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductOptionCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionUpdateRequest;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import org.springframework.stereotype.Component;

@Component
public class ProductOptionHelper {

    public ProductOption createProductOption(ProductOptionCreateRequest request) {
        return ProductOption.builder()
                .name(request.name())
                .build();
    }

    public void updateProductOption(ProductOptionUpdateRequest request, ProductOption option) {
        option.setName(request.name());
    }
}
