package com.yas.system.auth.internal.entity;

import com.yas.system.auth.internal.enumeration.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(
        name = "tbl_customer_profile",
        uniqueConstraints = @UniqueConstraint(name = "uk_customer_profile_name", columnNames = "name")
)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomerProfile {

    @Id
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 1024)
    private String avatar;
}
