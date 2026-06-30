package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.ProductRequest;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.service.ProductService;
import com.yas.system.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    ProductService productService;

    @PostMapping()
    public ApiResponse<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ApiResponse.success(product);
    }
//
//
//    @GetMapping("/{productId}")
//    public ApiResponse<ProductResponse> getProduct(@PathVariable String productId) {
//        return ApiResponse.success(productService.getById(productId));
//    }
//
//    @GetMapping("/variants/{id}")
//    public ApiResponse<ProductVariantResponse> getProductVariants(@PathVariable String id) {
//        return ApiResponse.success(productService.getProductVariant(id));
//    }
//
//
//    @PostMapping("/attribute")
//    public ApiResponse<Void> createAttribute(@RequestBody @Valid AttributeRequest request) {
//        productService.createAttribute(request);
//        return ApiResponse.successWithNoContent();
//    }
}
