package com.yas.system.catalog.internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attributes")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attribute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String dataType;  // string, int, boolean...
    private String unit;      // "cm", "kg", etc.
}
