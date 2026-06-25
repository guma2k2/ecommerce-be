package com.yas.system.catalog.internal.repository;

import com.tiki.product.entity.ProductAttributeValue;
import com.tiki.product.entity.VariantAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {

    @Query("""
        select vav 
        from ProductAttributeValue vav
        join fetch vav.attribute
        join fetch vav.product p 
        where p.id = :id
    """)
    List<ProductAttributeValue> findByProduct(Long id);
}
