package com.yas.system.catalog.internal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_product_variant")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String sku;
    private BigDecimal price;
    private Integer quantity;
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "productVariant", cascade = CascadeType.ALL)
    private List<VariantOptionValue> attributeValues = new ArrayList<>();
}
