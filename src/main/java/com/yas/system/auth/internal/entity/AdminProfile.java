package com.yas.system.auth.internal.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "tbl_admin_profile")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminProfile {
    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String name;

    @Column(length = 1024)
    private String avatar;
}
