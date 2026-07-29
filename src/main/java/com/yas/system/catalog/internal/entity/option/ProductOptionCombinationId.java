package com.yas.system.catalog.internal.entity.option;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class ProductOptionCombinationId implements Serializable {

    private Long productId;
    private Long productOptionId;

    public ProductOptionCombinationId() {}

    public ProductOptionCombinationId(Long productId, Long productOptionId) {
        this.productId = productId;
        this.productOptionId = productOptionId;
    }

}