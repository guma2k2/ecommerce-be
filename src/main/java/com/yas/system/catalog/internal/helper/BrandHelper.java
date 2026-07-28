package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.BrandCreateRequest;
import com.yas.system.catalog.internal.dto.request.BrandUpdateRequest;
import com.yas.system.catalog.internal.entity.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandHelper {

    public Brand createBrand(BrandCreateRequest request) {
        return Brand.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    public void updateBrand(BrandUpdateRequest request, Brand brand) {
        brand.setName(request.name());
        brand.setDescription(request.description());
    }
}
