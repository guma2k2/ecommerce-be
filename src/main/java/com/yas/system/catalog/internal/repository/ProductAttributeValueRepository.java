package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {

    @Query("""
        select pav
        from ProductAttributeValue pav
        join fetch pav.product p
        join fetch pav.productAttribute pa
        where p.id = :productId
    """)
    List<ProductAttributeValue> findByProductId(Long productId);

    @Modifying
    @Query("""
        delete from ProductAttributeValue pav
        where pav.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);
}
