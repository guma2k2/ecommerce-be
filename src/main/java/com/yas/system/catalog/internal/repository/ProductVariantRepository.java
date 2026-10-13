package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Query("""
        select pv
        from ProductVariant pv
        left join fetch pv.product p
        where pv.id = :id
    """)
    Optional<ProductVariant> findByIdWithProduct(@Param("id") Long id);

    @Query("""
        select pv
        from ProductVariant pv
        left join fetch pv.product p
        where pv.id in :variantIds
    """)
    List<ProductVariant> findByIdInWithProduct(@Param("variantIds") Collection<Long> variantIds);

    List<ProductVariant> findByProductId(Long productId);

    List<ProductVariant> findByProductIdIn(List<Long> productIds);

    @Modifying
    @Query("""
        delete from ProductVariant pv
        where pv.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);
}
