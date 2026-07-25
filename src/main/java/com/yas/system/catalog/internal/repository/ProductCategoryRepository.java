package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.ProductCategory;
import com.yas.system.catalog.internal.entity.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {

    List<ProductCategory> findByProductId(Long productId);

    @Modifying
    @Query("""
        delete from ProductCategory pc
        where pc.product.id = :productId
    """)
    void deleteByProductId(@Param("productId") Long productId);
}
