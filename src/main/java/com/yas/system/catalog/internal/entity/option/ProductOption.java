package com.yas.system.catalog.internal.entity.option;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_product_option")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductOption extends BaseLongEntity {

    private String name;

}
