package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductUpdateRequest;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductHelper {

    public Product createProduct(ProductCreateRequest request, Category category, Brand brand) {
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .slug(request.slug())
                .metaTitle(request.metaTitle())
                .metaKeyword(request.metaKeyword())
                .metaDescription(request.metaDescription())
                .category(category)
                .brand(brand)
                .build();
    }

    public void updateProduct(ProductUpdateRequest request, Product product, Category category, Brand brand) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setSlug(request.slug());
        product.setMetaTitle(request.metaTitle());
        product.setMetaKeyword(request.metaKeyword());
        product.setMetaDescription(request.metaDescription());
        product.setCategory(category);
        product.setBrand(brand);
    }
}


