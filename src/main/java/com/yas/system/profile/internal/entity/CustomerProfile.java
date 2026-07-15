package com.yas.system.profile.internal.entity;

import com.yas.system.common.entity.BaseUuidEntity;
import com.yas.system.profile.internal.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "tbl_customer")
@Entity
@NoArgsConstructor
@Getter
@Setter
public class CustomerProfile extends BaseUuidEntity {

    @Column(nullable = false, unique = true)
    private String userId;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String language;
}
