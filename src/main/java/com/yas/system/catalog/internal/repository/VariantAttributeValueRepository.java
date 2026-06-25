package com.yas.system.catalog.internal.repository;

import com.tiki.product.entity.VariantAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantAttributeValueRepository extends JpaRepository<VariantAttributeValue, Long> {



    @Query("""
        select vav 
        from VariantAttributeValue vav
        join fetch vav.attribute
        join fetch vav.productVariant p 
        where p.id = :id
    """)
    List<VariantAttributeValue> findByProductVariant(Long id);



}
