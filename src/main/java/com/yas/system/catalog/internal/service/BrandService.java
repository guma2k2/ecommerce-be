package com.yas.system.catalog.internal.service;


import com.yas.system.catalog.internal.dto.request.BrandCreateRequest;
import com.yas.system.catalog.internal.dto.request.BrandUpdateRequest;
import com.yas.system.catalog.internal.dto.response.BrandResponse;
import com.yas.system.common.response.PageResponse;

public interface BrandService {
    void createBrand(BrandCreateRequest request);
    void updateBrand(BrandUpdateRequest request, Integer brandId);
    void deleteBrandById(Integer brandId);
    BrandResponse getById(Integer brandId);
    PageResponse<BrandResponse> getBrandPage(Integer pageNumber, Integer pageSize);
}

