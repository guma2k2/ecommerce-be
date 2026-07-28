package com.yas.system.auth.internal.entity;

import com.yas.system.common.entity.BaseIntegerEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tbl_role")
@NamedEntityGraph(
        name = "Role.permissions",
        attributeNodes = @NamedAttributeNode("permissions")
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Role extends BaseIntegerEntity {
    @Column(nullable = false, unique = true)
    private String name;

    private boolean isAllowGetAll;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}
