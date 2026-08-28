package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantAttributeValueRepository extends JpaRepository<ProductVariantAttributeValue, Long> {

    @Query("""
        select vav
        from ProductVariantAttributeValue vav
        join fetch vav.productVariant pv
        join fetch vav.productAttribute pa
        where pv.product.id = :productId
    """)
    List<ProductVariantAttributeValue> findByProductVariantProductId(@Param("productId") Long productId);

    List<ProductVariantAttributeValue> findByProductVariantId(Long variantId);

    @Modifying
    @Query("""
        delete from ProductVariantAttributeValue vav
        where vav.productVariant.product.id = :productId
    """)
    void deleteByProductVariantProductId(@Param("productId") Long productId);

    void deleteByProductVariantId(Long variantId);
}
