package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.option.ProductOptionCombination;
import com.yas.system.catalog.internal.entity.option.ProductOptionCombinationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionCombinationRepository extends JpaRepository<ProductOptionCombination, ProductOptionCombinationId> {

    @Query("""
        select poc
        from ProductOptionCombination poc
        join fetch poc.productOption po
        where poc.product.id = :productId
        order by poc.position asc
    """)
    List<ProductOptionCombination> findByProductIdOrderByPositionAsc(@Param("productId") Long productId);

    @Modifying
    @Query("""
        delete from ProductOptionCombination poc
        where poc.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);
}
