package com.yas.system.catalog.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_attributes")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryAttribute extends BaseLongEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private ProductOption productOption;

    private Boolean isRequired;

    private Boolean isFilterable;
}
