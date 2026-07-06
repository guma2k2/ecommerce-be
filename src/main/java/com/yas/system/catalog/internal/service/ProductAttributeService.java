package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.ProductAttributeRequest;
import com.yas.system.catalog.internal.dto.response.ProductAttributeResponse;

public interface ProductAttributeService {
    void createProductAttribute(ProductAttributeRequest request);
    void updateProductAttribute(ProductAttributeRequest request, Long productAttributeId);
    void deleteProductAttributeById(Long productAttributeId);
    ProductAttributeResponse getById(Long productAttributeId);
}
