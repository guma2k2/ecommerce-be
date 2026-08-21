package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.attribute.ProductAttributeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeTemplateRepository extends JpaRepository<ProductAttributeTemplate, Long> {

    @Query("""
        select pat
        from ProductAttributeTemplate pat
        join fetch pat.productAttribute pa
        where pat.productTemplate.id = :productTemplateId
        order by pat.position asc
    """)
    List<ProductAttributeTemplate> findByProductTemplateId(@Param("productTemplateId") Integer productTemplateId);

    @Modifying
    @Query("""
        delete from ProductAttributeTemplate pat
        where pat.productTemplate.id = :productTemplateId
    """)
    void deleteByProductTemplateId(@Param("productTemplateId") Integer productTemplateId);
}
