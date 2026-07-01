package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductRequest;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.service.ProductService;
import com.yas.system.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ProductController {

    ProductService productService;

    @PostMapping()
    public ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ApiResponse.success(product);
    }

    @PutMapping("/{productId}")
    public ApiResponse<ProductResponse> updateProduct(
            @RequestBody @Valid ProductRequest request,
            @PathVariable Long productId
    ) {
        ProductResponse product = productService.updateProduct(request, productId);
        return ApiResponse.success(product);
    }

    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable Long productId) {
        return ApiResponse.success(productService.getById(productId));
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteById(productId);
        return ApiResponse.successWithNoContent();
    }

}
