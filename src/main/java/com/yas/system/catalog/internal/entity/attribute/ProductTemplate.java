package com.yas.system.catalog.internal.entity.attribute;

import com.yas.system.common.entity.BaseIntegerEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "tbl_product_template",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_template_name", columnNames = "name")
)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductTemplate extends BaseIntegerEntity {
    @Column(nullable = false, length = 100)
    private String name;
}
