package com.yas.system.catalog.internal.repository;


import com.yas.system.catalog.internal.entity.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Integer> {


    @Query("""
        select ca 
        from CategoryAttribute ca 
        join fetch ca.category c
        join fetch ca.attribute a 
        where c.id = :categoryId
    """)
    List<CategoryAttribute> findByCategory(Integer categoryId);
}
