package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductAttributeRequest;
import com.yas.system.catalog.internal.dto.response.ProductAttributeResponse;
import com.yas.system.catalog.internal.service.ProductAttributeService;
import com.yas.system.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-attributes")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductAttributeController {

    ProductAttributeService productAttributeService;

    @PostMapping()
    public ApiResponse<Void> createProductAttribute(@RequestBody @Valid ProductAttributeRequest request) {
        productAttributeService.createProductAttribute(request);
        return ApiResponse.successWithNoContent();
    }

    @PutMapping("/{productAttributeId}")
    public ApiResponse<Void> updateProductAttribute(
            @RequestBody @Valid ProductAttributeRequest request,
            @PathVariable Long productAttributeId
    ) {
        productAttributeService.updateProductAttribute(request, productAttributeId);
        return ApiResponse.successWithNoContent();
    }

    @GetMapping("/{productAttributeId}")
    public ApiResponse<ProductAttributeResponse> getProductAttribute(@PathVariable Long productAttributeId) {
        return ApiResponse.success(productAttributeService.getById(productAttributeId));
    }

    @DeleteMapping("/{productAttributeId}")
    public ApiResponse<Void> deleteProductAttribute(@PathVariable Long productAttributeId) {
        productAttributeService.deleteProductAttributeById(productAttributeId);
        return ApiResponse.successWithNoContent();
    }
}
