package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductOptionCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductOptionResponse;
import com.yas.system.catalog.internal.service.ProductOptionService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-options")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductOptionController {

    ProductOptionService productOptionService;

    @PostMapping()
    public ApiResponse<Void> createProductOption(@RequestBody @Valid ProductOptionCreateRequest request) {
        productOptionService.createProductOption(request);
        return ApiResponse.successWithNoContent();
    }

    @PutMapping("/{productOptionId}")
    public ApiResponse<Void> updateProductOption(
            @RequestBody @Valid ProductOptionUpdateRequest request,
            @PathVariable Long productOptionId
    ) {
        productOptionService.updateProductOption(request, productOptionId);
        return ApiResponse.successWithNoContent();
    }

    @GetMapping("/{productOptionId}")
    public ApiResponse<ProductOptionResponse> getProductOption(@PathVariable Long productOptionId) {
        return ApiResponse.success(productOptionService.getById(productOptionId));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<ProductOptionResponse>> getProductOptionPage(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(productOptionService.getProductOptionPage(pageNumber, pageSize));
    }

    @DeleteMapping("/{productOptionId}")
    public ApiResponse<Void> deleteProductOption(@PathVariable Long productOptionId) {
        productOptionService.deleteProductOptionById(productOptionId);
        return ApiResponse.successWithNoContent();
    }
}
