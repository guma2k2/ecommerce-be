package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.ProductAttributeCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductAttributeUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductAttributeResponse;
import com.yas.system.common.response.PageResponse;

public interface ProductAttributeService {
    ProductAttributeResponse createProductAttribute(ProductAttributeCreateRequest request);
    ProductAttributeResponse updateProductAttribute(ProductAttributeUpdateRequest request, Long productAttributeId);
    void deleteProductAttributeById(Long productAttributeId);
    ProductAttributeResponse getById(Long productAttributeId);
    PageResponse<ProductAttributeResponse> getProductAttributePage(Integer pageNumber, Integer pageSize);
}

