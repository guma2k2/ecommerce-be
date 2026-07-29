package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.option.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    Optional<ProductOption> findByName(String name);

    @Query("""
        select p 
        from ProductOption p 
        where p.name = :name and (p.id != :id or :id is null)
    """)
    Optional<ProductOption> checkExited(String name, Long id);
}
