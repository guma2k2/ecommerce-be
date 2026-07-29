package com.yas.system.catalog.internal.entity;

import com.yas.system.catalog.internal.entity.productCategory.ProductCategory;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tbl_product")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends BaseLongEntity {

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 60)
    private String metaTitle;

    private String metaKeyword;

    @Column(length = 160)
    private String metaDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
    private List<ProductVariant> productVariants = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private Set<ProductCategory> productCategories;
}
