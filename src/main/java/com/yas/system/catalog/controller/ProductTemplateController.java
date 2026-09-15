package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductTemplateCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductTemplateUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductTemplateResponse;
import com.yas.system.catalog.internal.service.ProductTemplateService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
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
    public ApiResponse<ProductTemplateResponse> createProductTemplate(@RequestBody @Valid ProductTemplateCreateRequest request) {
        return ApiResponse.success(productTemplateService.createProductTemplate(request));
    }

    @PutMapping("/{productTemplateId}")
    public ApiResponse<ProductTemplateResponse> updateProductTemplate(
            @RequestBody @Valid ProductTemplateUpdateRequest request,
            @PathVariable Integer productTemplateId
    ) {
        return ApiResponse.success(productTemplateService.updateProductTemplate(request, productTemplateId));
    }

    @GetMapping("/{productTemplateId}")
    public ApiResponse<ProductTemplateResponse> getProductTemplate(@PathVariable Integer productTemplateId) {
        return ApiResponse.success(productTemplateService.getById(productTemplateId));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<ProductTemplateResponse>> getProductTemplatePage(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "is_include_attributes", defaultValue = "false") Boolean isIncludeAttributes
    ) {
        return ApiResponse.success(productTemplateService.getProductTemplatePage(pageNumber, pageSize, isIncludeAttributes));
    }

    @DeleteMapping("/{productTemplateId}")
    public ApiResponse<Void> deleteProductTemplate(@PathVariable Integer productTemplateId) {
        productTemplateService.deleteProductTemplateById(productTemplateId);
        return ApiResponse.successWithNoContent();
    }
}

