package com.yas.system.catalog.internal.repository;

import com.tiki.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {

    @Query("""
        select c 
        from Brand c 
        where c.name = :name and (c.id != :id or :id is null)
        """)
    Optional<Brand> checkExited(String name, Integer id);

}
