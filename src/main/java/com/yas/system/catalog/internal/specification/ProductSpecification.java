package com.yas.system.catalog.internal.specification;

import com.yas.system.catalog.internal.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.Objects;

public class ProductSpecification {

    public static Specification<Product> hasName (String name) {
        return (root, query, criteriaBuilder) -> {
            if  (Objects.isNull(name)) {
                return null;
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }
}
