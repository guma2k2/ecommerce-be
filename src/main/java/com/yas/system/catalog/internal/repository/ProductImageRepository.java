package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("""
        select pi 
        from ProductImage pi 
        join fetch pi.product p 
        join fetch pi.productVariant pv
        where p.id = :productId
    """)
    List<ProductImage> findByProductId(Long productId);
}
