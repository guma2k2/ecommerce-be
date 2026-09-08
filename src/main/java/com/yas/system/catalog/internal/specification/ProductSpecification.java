package com.yas.system.catalog.internal.specification;

import com.yas.system.catalog.internal.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public class ProductSpecification {

    public static Specification<Product> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (Objects.isNull(name) || name.isBlank()) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasCategoryId(Integer categoryId) {
        return (root, query, criteriaBuilder) -> {
            if (Objects.isNull(categoryId)) {
                return null;
            }
            return criteriaBuilder.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> hasBrandId(Integer brandId) {
        return (root, query, criteriaBuilder) -> {
            if (Objects.isNull(brandId)) {
                return null;
            }
            return criteriaBuilder.equal(root.get("brand").get("id"), brandId);
        };
    }
}
