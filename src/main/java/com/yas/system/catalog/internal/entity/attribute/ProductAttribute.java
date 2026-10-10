package com.yas.system.catalog.internal.entity.attribute;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;

@Entity
@Table(
        name = "tbl_product_attribute",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_attribute_name", columnNames = "name")
)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductAttribute extends BaseLongEntity {

    @Column(nullable = false, length = 100)
    private String name;

}
