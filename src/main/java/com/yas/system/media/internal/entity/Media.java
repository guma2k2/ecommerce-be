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

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1024)
    private String url;

    @Builder.Default
    private boolean active = false;

    private long size;

    @Column(length = 500)
    private String altText;

    @Column(length = 20)
    private String duration;

    @Column(length = 20)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private MediaType type;
}
