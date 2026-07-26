package com.yas.system.catalog.internal.service;


import com.yas.system.catalog.internal.dto.request.BrandRequest;
import com.yas.system.catalog.internal.dto.response.BrandResponse;
import com.yas.system.common.response.PageResponse;

public interface BrandService {
    void createBrand(BrandRequest request);
    void updateBrand(BrandRequest request, Long brandId);
    void deleteBrandById(Long brandId);
    BrandResponse getById(Long brandId);
    PageResponse<BrandResponse> getBrandPage(Integer pageNumber, Integer pageSize);
}

