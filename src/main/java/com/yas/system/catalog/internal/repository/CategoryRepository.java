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
        select distinct c 
        from Category c 
        left join fetch c.children 
        where c.parent is null 
        """)
    List<Category> findAllCategoryParents();

    @Query("""
        select c 
        from Category c 
        left join fetch c.children 
        where lower(c.name) = lower(:name)
        """)
    Optional<Category> findByNameIgnoreCaseCustom(String name);

    @Query("""
        select c 
        from Category c
        left join fetch c.children
        where c.id = :id 
    """)
    Optional<Category> findByIdCustom(Integer id);

    @Query(value = """
        WITH RECURSIVE category_tree AS (
            SELECT id FROM tbl_category WHERE id = :categoryId
            UNION ALL
            SELECT c.id FROM tbl_category c
            INNER JOIN category_tree ct ON c.parent_id = ct.id
        )
        SELECT id FROM category_tree
    """, nativeQuery = true)
    List<Integer> findCategoryAndDescendantIds(@org.springframework.data.repository.query.Param("categoryId") Integer categoryId);

    Optional<Category> findByName(String name);
}
