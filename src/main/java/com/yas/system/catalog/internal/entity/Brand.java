package com.yas.system.catalog.internal.entity;


import com.yas.system.common.entity.BaseIntegerEntity;
import com.yas.system.common.entity.BaseLongEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_brand")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Brand extends BaseIntegerEntity {

    @Column(length = 50, unique = true, nullable = false)
    private String name;

    @Column(length = 200)
    private String description;
}
