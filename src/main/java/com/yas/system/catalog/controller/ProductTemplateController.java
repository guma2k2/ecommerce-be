package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductTemplateRequest;
import com.yas.system.catalog.internal.dto.response.ProductTemplateResponse;
import com.yas.system.catalog.internal.service.ProductTemplateService;
import com.yas.system.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-templates")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductTemplateController {

    ProductTemplateService productTemplateService;

    @PostMapping()
    public ApiResponse<Void> createProductTemplate(@RequestBody @Valid ProductTemplateRequest request) {
        productTemplateService.createProductTemplate(request);
        return ApiResponse.successWithNoContent();
    }

    @PutMapping("/{productTemplateId}")
    public ApiResponse<Void> updateProductTemplate(
            @RequestBody @Valid ProductTemplateRequest request,
            @PathVariable Integer productTemplateId
    ) {
        productTemplateService.updateProductTemplate(request, productTemplateId);
        return ApiResponse.successWithNoContent();
    }

    @GetMapping("/{productTemplateId}")
    public ApiResponse<ProductTemplateResponse> getProductTemplate(@PathVariable Integer productTemplateId) {
        return ApiResponse.success(productTemplateService.getById(productTemplateId));
    }

    @DeleteMapping("/{productTemplateId}")
    public ApiResponse<Void> deleteProductTemplate(@PathVariable Integer productTemplateId) {
        productTemplateService.deleteProductTemplateById(productTemplateId);
        return ApiResponse.successWithNoContent();
    }
}
