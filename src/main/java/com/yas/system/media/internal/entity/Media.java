package com.yas.system.media.internal.entity;

import com.yas.system.common.entity.BaseUuidEntity;
import com.yas.system.media.internal.enums.MediaType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "tbl_medias")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Media extends BaseUuidEntity {
    private String url;

    private boolean active = false;

    private String duration;

    @Enumerated(EnumType.STRING)
    private MediaType type;
}
