package com.yas.system.catalog.internal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "variant_attribute_values")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariantAttributeValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;

    @ManyToOne
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @ManyToOne
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;
}
