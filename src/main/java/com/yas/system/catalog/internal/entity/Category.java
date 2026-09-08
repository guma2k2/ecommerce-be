package com.yas.system.catalog.internal.entity;

import com.yas.system.common.entity.BaseIntegerEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_category")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category extends BaseIntegerEntity {

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", orphanRemoval = true)
    private List<Category> children;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();
}
