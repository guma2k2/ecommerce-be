package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.ProductRequest;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.entity.Product;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(ProductRequest request, Long productId);
    ProductResponse getById(Long id );
    void deleteById(Long id);
}
