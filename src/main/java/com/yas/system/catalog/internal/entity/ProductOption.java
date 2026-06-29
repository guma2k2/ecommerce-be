package com.yas.system.catalog.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "tbl_product_option")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductOption extends BaseLongEntity {

    private String name;

    private int position;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "values", columnDefinition = "text[]")
    private List<String> values;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}
