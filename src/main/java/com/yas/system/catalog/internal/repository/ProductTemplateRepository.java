package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.attribute.ProductTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, Integer> {

    @Query("""
        select pt
        from ProductTemplate pt
        where pt.name = :name and (pt.id != :id or :id is null)
    """)
    Optional<ProductTemplate> checkExited(String name, Integer id);
}
