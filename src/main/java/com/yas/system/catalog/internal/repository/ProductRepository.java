package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {


    @Query("""
        select p 
        from Product p
        join fetch p.category 
        join fetch p.brand
        left join fetch p.productVariants
        where p.id = :id
    """)
    Optional<Product> findByIdCustom(Long id);
}
