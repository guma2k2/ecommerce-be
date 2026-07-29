package com.yas.system.catalog.internal.entity.productCategory;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
public class ProductCategoryId implements Serializable {

    private Long productId;
    private Integer categoryId;

    public ProductCategoryId() {}

    public ProductCategoryId(Long productId, Integer categoryId) {
        this.productId = productId;
        this.categoryId = categoryId;
    }

}