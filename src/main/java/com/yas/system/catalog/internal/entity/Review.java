package com.yas.system.catalog.internal.entity;

import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_review")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review extends BaseLongEntity {

    private String content;

    private int ratingStar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    private String customerId;

}
