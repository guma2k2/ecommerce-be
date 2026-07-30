package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.option.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, Long> {

    @Query("""
        select pov
        from ProductOptionValue pov
        join fetch pov.productOptionCombination poc
        join fetch poc.productOption po
        where poc.product.id = :productId
        order by pov.position asc
    """)
    List<ProductOptionValue> findByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("""
        delete from ProductOptionValue pov
        where pov.productOptionCombination.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);
}
