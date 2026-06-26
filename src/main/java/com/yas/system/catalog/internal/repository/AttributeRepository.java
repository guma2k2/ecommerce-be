package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Integer> {

    Optional<Attribute> findByName(String name);
}
