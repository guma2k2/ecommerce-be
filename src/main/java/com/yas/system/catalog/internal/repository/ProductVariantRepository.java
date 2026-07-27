package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    @Modifying
    @Query("""
        delete from ProductVariant pv
        where pv.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);
}
