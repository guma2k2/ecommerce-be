package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Optional<Product> findBySlug(String slug);

    @Query(value = """
        SELECT id FROM tbl_product
        ORDER BY RANDOM()
        LIMIT :limit
    """, nativeQuery = true)
    List<Long> findRandomProductIds(@Param("limit") int limit);

    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN FETCH p.brand
        LEFT JOIN FETCH p.category
        WHERE p.id IN :ids
    """)
    List<Product> findByIdInWithBrandAndCategory(@Param("ids") List<Long> ids);
}
