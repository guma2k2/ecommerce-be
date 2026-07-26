package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.ProductTemplateRequest;
import com.yas.system.catalog.internal.dto.response.ProductTemplateResponse;
import com.yas.system.common.response.PageResponse;

public interface ProductTemplateService {
    void createProductTemplate(ProductTemplateRequest request);
    void updateProductTemplate(ProductTemplateRequest request, Integer productTemplateId);
    void deleteProductTemplateById(Integer productTemplateId);
    ProductTemplateResponse getById(Integer productTemplateId);
    PageResponse<ProductTemplateResponse> getProductTemplatePage(Integer pageNumber, Integer pageSize);
}

