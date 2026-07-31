package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.ProductCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.dto.response.ProductThumbnailResponse;
import com.yas.system.common.response.PageResponse;

public interface ProductService {
    ProductResponse createProduct(ProductCreateRequest request);
    ProductResponse updateProduct(ProductUpdateRequest request, Long productId);
    ProductResponse getById(Long id);
    void deleteById(Long id);
    PageResponse<ProductThumbnailResponse> getProducts(
            int pageNo,
            int pageSize,
            String name,
            Integer categoryId,
            Integer brandId
    );
}
