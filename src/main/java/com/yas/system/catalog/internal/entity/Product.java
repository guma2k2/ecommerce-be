package com.yas.system.catalog.internal.entity;

import com.yas.system.catalog.api.enumeration.ProductStatus;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tbl_product",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_name", columnNames = "name"),
                @UniqueConstraint(name = "uk_product_slug", columnNames = "slug")
        }
)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends BaseLongEntity {

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 60)
    private String metaTitle;

    @Column(length = 200)
    private String metaKeyword;

    @Column(length = 160)
    private String metaDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @ColumnDefault("'DRAFT'")
    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "product")
    private List<ProductVariant> productVariants = new ArrayList<>();

    public boolean isActive() {
        return ProductStatus.ACTIVE.equals(this.status);
    }
}
