package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductAttributeCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductAttributeUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductAttributeResponse;
import com.yas.system.catalog.internal.service.ProductAttributeService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
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
    public ApiResponse<ProductAttributeResponse> createProductAttribute(@RequestBody @Valid ProductAttributeCreateRequest request) {
        return ApiResponse.success(productAttributeService.createProductAttribute(request));
    }

    @PutMapping("/{productAttributeId}")
    public ApiResponse<ProductAttributeResponse> updateProductAttribute(
            @RequestBody @Valid ProductAttributeUpdateRequest request,
            @PathVariable Long productAttributeId
    ) {
        return ApiResponse.success(productAttributeService.updateProductAttribute(request, productAttributeId));
    }

    @GetMapping("/{productAttributeId}")
    public ApiResponse<ProductAttributeResponse> getProductAttribute(@PathVariable Long productAttributeId) {
        return ApiResponse.success(productAttributeService.getById(productAttributeId));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<ProductAttributeResponse>> getProductAttributePage(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(productAttributeService.getProductAttributePage(pageNumber, pageSize));
    }

    @DeleteMapping("/{productAttributeId}")
    public ApiResponse<Void> deleteProductAttribute(@PathVariable Long productAttributeId) {
        productAttributeService.deleteProductAttributeById(productAttributeId);
        return ApiResponse.successWithNoContent();
    }
}

