package com.yas.system.auth.internal.entity;

import com.yas.system.auth.internal.enums.OauthProvider;
import com.yas.system.common.entity.BaseUuidEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tbl_user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class User extends BaseUuidEntity {

    @Column(unique = true)
    private String email;

    private String password;

    private String name;

    private boolean isVerified;

    @Enumerated(EnumType.STRING)
    private OauthProvider provider;

    private boolean isEnabledMfa;

    private String mfaSecret;

    private int role;

}
