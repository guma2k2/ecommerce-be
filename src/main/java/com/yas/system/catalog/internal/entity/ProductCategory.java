package com.yas.system.catalog.internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_product_category")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductCategory {

    @EmbeddedId
    private ProductCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    private Category category;

}