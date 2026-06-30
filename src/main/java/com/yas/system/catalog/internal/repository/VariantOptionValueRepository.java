package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.VariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantOptionValueRepository extends JpaRepository<VariantOptionValue, Long> {
}
