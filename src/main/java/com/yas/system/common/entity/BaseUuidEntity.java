package com.yas.system.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;


@MappedSuperclass
@Getter
public abstract class BaseUuidEntity extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}