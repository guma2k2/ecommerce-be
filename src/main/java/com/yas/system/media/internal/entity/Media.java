package com.yas.system.media.internal.entity;

import com.yas.system.common.entity.BaseUuidEntity;
import com.yas.system.media.internal.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_medias")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Media extends BaseUuidEntity {

    @Column(nullable = false)
    private String name;

    private String url;

    @Builder.Default
    private boolean active = false;

    private long size;

    private String altText;

    private String duration;

    @Column(length = 20)
    private String fileType;

    @Enumerated(EnumType.STRING)
    private MediaType type;
}
