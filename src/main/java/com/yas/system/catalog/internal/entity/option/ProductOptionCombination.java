package com.yas.system.catalog.internal.entity.option;

import com.yas.system.catalog.internal.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_product_option_combination")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductOptionCombination {

    @EmbeddedId
    private ProductOptionCombinationId id;

    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productId")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productOptionId")
    private ProductOption productOption;

}