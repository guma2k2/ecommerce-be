package com.yas.system.catalog.internal.service;


import com.yas.system.catalog.internal.dto.request.BrandCreateRequest;
import com.yas.system.catalog.internal.dto.request.BrandUpdateRequest;
import com.yas.system.catalog.internal.dto.response.BrandResponse;
import com.yas.system.common.response.PageResponse;

public interface BrandService {
    void createBrand(BrandCreateRequest request);
    void updateBrand(BrandUpdateRequest request, Long brandId);
    void deleteBrandById(Long brandId);
    BrandResponse getById(Long brandId);
    PageResponse<BrandResponse> getBrandPage(Integer pageNumber, Integer pageSize);
}

