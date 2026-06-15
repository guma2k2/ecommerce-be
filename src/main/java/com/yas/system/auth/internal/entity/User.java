package com.yas.system.auth.internal.entity;

import com.yas.system.auth.internal.enums.OauthProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String email;

    private String password;

    private String name;

    private boolean isVerified;

    @Enumerated(EnumType.STRING)
    private OauthProvider provider;

    private String mfaSecret;

    private int role;

}
