package com.yas.system.catalog.internal.entity.attribute;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "tbl_product_template")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductTemplate extends BaseLongEntity {
    @Column(nullable = false, unique = true)
    private String name;
}
