package com.yas.system.auth.internal.entity;

import com.yas.system.auth.internal.enumeration.ApiMethod;
import com.yas.system.auth.internal.enumeration.ApiModule;
import com.yas.system.common.entity.BaseIntegerEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tbl_permission")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Permission extends BaseIntegerEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String api;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ApiMethod method;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ApiModule module;
}
