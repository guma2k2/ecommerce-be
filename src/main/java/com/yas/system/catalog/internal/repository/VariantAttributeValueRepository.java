package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.VariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantAttributeValueRepository extends JpaRepository<VariantOptionValue, Long> {



    @Query("""
        select vav 
        from VariantOptionValue vav
        join fetch vav.attribute
        join fetch vav.productVariant p 
        where p.id = :id
    """)
    List<VariantOptionValue> findByProductVariant(Long id);



}
