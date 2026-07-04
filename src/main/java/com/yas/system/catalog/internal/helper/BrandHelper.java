package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.BrandRequest;
import com.yas.system.catalog.internal.entity.Brand;
import org.springframework.stereotype.Component;

@Component
public class BrandHelper {

    public Brand createBrand(BrandRequest request) {
        return Brand.builder()
                .name(request.name())
                .description(request.description())
                .build();
    }

    public void updateBrand(BrandRequest request, Brand brand) {
        brand.setName(request.name());
        brand.setDescription(request.description());
    }
}
