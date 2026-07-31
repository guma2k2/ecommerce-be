package com.yas.system.catalog.internal.entity.option;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_product_option_value")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductOptionValue extends BaseLongEntity {

    @Column(nullable = false)
    private String value;

    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "product_id"),
            @JoinColumn(name = "product_option_id")
    })
    private ProductOptionCombination productOptionCombination;
}
