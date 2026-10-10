package com.yas.system.auth.internal.entity;

import com.yas.system.auth.internal.enumeration.OauthProvider;
import com.yas.system.common.entity.BaseUuidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "tbl_user",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email")
)
@NamedEntityGraph(
        name = "User.roles",
        attributeNodes = @NamedAttributeNode(value = "roles", subgraph = "role.permissions"),
        subgraphs = @NamedSubgraph(
                name = "role.permissions",
                attributeNodes = @NamedAttributeNode("permissions")
        )
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User extends BaseUuidEntity {

    @Column(length = 254)
    private String email;

    @Column(length = 60)
    private String password;

//    private String name;

    private boolean isVerified;

    @Column(length = 10)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OauthProvider provider;

    @Column(columnDefinition = "boolean default false")
    private boolean isEnabledMfa = false;

    @Column(length = 64)
    private String mfaSecret;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

}
