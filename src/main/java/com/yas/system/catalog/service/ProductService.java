package com.yas.system.catalog.service;

import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.catalog.internal.dto.request.ProductCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.dto.response.ProductSearchItemResponse;
import com.yas.system.catalog.internal.dto.response.ProductThumbnailResponse;
import com.yas.system.common.response.PageResponse;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    List<ProductSearchItemResponse> getBestSellers(Integer limit);
    ProductResponse getBySlug(String slug);

    ProductVariantPublicDto getProductVariantById(Long variantId);
    Map<Long, ProductVariantPublicDto> getProductVariantsByIds(Collection<Long> variantIds);
    void deductStock(Map<Long, Integer> variantQuantities);
    void restoreStock(Map<Long, Integer> variantQuantities);
}
