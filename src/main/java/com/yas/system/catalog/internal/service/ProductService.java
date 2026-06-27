package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.AttributeRequest;
import com.yas.system.catalog.internal.dto.request.ProductCreationRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantCreateRequest;
import com.yas.system.catalog.internal.dto.response.ProductImageResponse;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.dto.response.ProductVariantResponse;
import com.yas.system.catalog.internal.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductService {
    ProductResponse findById(String id);
    Map<String, String> getProductAttributeValues(Product product);
    List<ProductImageResponse> getProductImages(Product product);
    List<ProductVariantResponse> getProductVariants(Product product);
    void createProduct(ProductCreationRequest request);
    void saveProductVariants(List<ProductVariantCreateRequest> requests, Long productId);
    void createAttribute(AttributeRequest request);
    ProductResponse getById(String productId);
    ProductVariantResponse getProductVariant(String id);
}
