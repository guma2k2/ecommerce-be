package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    Optional<ProductOption> findByName(String name);

    List<ProductOption> findByProductId(Long productId);

    @Modifying
    @Query("""
        delete from ProductOption po
        where po.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);
}
