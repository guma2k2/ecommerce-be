package com.yas.system.catalog.internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_variant_option_value")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariantOptionValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private ProductOption productOption;
}
