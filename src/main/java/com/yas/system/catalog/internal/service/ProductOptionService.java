package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.ProductOptionCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductOptionResponse;
import com.yas.system.common.response.PageResponse;

public interface ProductOptionService {
    ProductOptionResponse createProductOption(ProductOptionCreateRequest request);
    ProductOptionResponse updateProductOption(ProductOptionUpdateRequest request, Long productOptionId);
    void deleteProductOptionById(Long productOptionId);
    ProductOptionResponse getById(Long productOptionId);
    PageResponse<ProductOptionResponse> getProductOptionPage(Integer pageNumber, Integer pageSize);
}
