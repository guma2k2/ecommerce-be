package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
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

    @Query("""
        select vov
        from VariantOptionValue vov
        join fetch vov.productVariant pv
        join fetch vov.productOptionValue pov
        join fetch pov.productOptionCombination poc
        join fetch poc.productOption po
        where pv.id in :variantIds
    """)
    List<VariantOptionValue> findByProductVariantIdInWithDetails(@Param("variantIds") Collection<Long> variantIds);

    @Modifying
    @Query("""
        delete from VariantOptionValue vov
        where vov.productVariant.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);

    @Modifying(flushAutomatically = true)
    @Query("""
        delete from VariantOptionValue vov
        where vov.productOptionValue.id in :productOptionValueIds
    """)
    void deleteByProductOptionValueIdIn(@Param("productOptionValueIds") Collection<Long> productOptionValueIds);
}
