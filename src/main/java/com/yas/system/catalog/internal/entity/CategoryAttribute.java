package com.yas.system.catalog.internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_attributes")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private Attribute attribute;

    private Boolean isRequired;

    private Boolean isFilterable;
}
