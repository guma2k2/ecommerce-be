package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductRequest;
import com.yas.system.catalog.internal.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductHelper {

    public Product createProduct(ProductRequest request) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .slug(request.slug())
                .metaTitle(request.metaTitle())
                .metaKeyword(request.metaKeyword())
                .metaDescription(request.metaDescription())
                .build();
    }

    public void updateProduct(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setSlug(request.slug());
        product.setMetaTitle(request.metaTitle());
        product.setMetaKeyword(request.metaKeyword());
        product.setMetaDescription(request.metaDescription());
    }
}


