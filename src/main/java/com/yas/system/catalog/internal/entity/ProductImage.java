package com.yas.system.catalog.internal.entity;

import com.yas.system.catalog.internal.dto.enums.ProductImageType;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_product_image")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImage extends BaseLongEntity {

    private String url;

    @Enumerated(EnumType.STRING)
    private ProductImageType type;

    @Builder.Default
    private int position = 0;

    private boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

}

//CREATE TABLE product_images (
//        id              BIGINT PRIMARY KEY AUTO_INCREMENT,
//        product_id      BIGINT NOT NULL,
//        variant_id      BIGINT DEFAULT NULL,
//        url             TEXT NOT NULL,
//        type            ENUM('THUMBNAIL', 'DETAIL') DEFAULT 'DETAIL',
//sort_order      INT DEFAULT 0,
//status          BOOLEAN DEFAULT TRUE,
//created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
//updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
//
//FOREIGN KEY (product_id) REFERENCES products(id),
//FOREIGN KEY (variant_id) REFERENCES product_variants(id)
//        );
