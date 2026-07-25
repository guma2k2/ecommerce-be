package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("""
        select c 
        from Category c 
        where c.name = :name and (c.id != :id or :id is null)
        """)
    Optional<Category> checkExited(String name, Integer id);


    @Query("""
        select c 
        from Category c 
        where c.parent is null 
        """)
    List<Category> findAllCategoryParents();

    @Query("""
        select c 
        from Category c
        left join fetch c.children
        where c.id = :id 
    """)
    Optional<Category> findByIdCustom(Integer id);
}
