package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariantOptionValueRepository extends JpaRepository<VariantOptionValue, Long> {

    @Query("""
        select vov
        from VariantOptionValue vov
        join fetch vov.productVariant pv
        join fetch vov.productOptionValue pov
        where pv.product.id = :productId
    """)
    List<VariantOptionValue> findByProductVariantProductId(@Param("productId") Long productId);

    @Modifying
    @Query("""
        delete from VariantOptionValue vov
        where vov.productVariant.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);
}
