package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {

    @Query("""
        select pa
        from ProductAttribute pa
        where pa.name = :name and (pa.id != :id or :id is null)
    """)
    Optional<ProductAttribute> checkExited(String name, Long id);
}
