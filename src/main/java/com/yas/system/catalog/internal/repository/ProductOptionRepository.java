package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Integer> {

    Optional<ProductOption> findByName(String name);
}
