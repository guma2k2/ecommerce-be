package com.yas.system.profile.internal.entity;

import com.yas.system.common.entity.BaseUuidEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Table(name = "tbl_admin")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Admin extends BaseUuidEntity {
}
