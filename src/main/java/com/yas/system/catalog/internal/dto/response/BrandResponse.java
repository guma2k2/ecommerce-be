package com.yas.system.catalog.internal.dto.response;


public record BrandResponse(
        Integer id,
        String name,
        String description
) {
    public static BrandResponse from(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName(), brand.getDescription());
    }
}
