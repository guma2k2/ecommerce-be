package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.repository.projection.AttributeFacetProjection;
import com.yas.system.catalog.internal.repository.projection.BrandFacetProjection;
import com.yas.system.catalog.internal.repository.projection.PriceRangeProjection;
import com.yas.system.catalog.internal.repository.projection.ProductSuggestionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    @Query(value = """
        SELECT 
            p.id AS id,
            p.name AS name,
            p.slug AS slug,
            c.name AS categoryName,
            (SELECT pm.media_id FROM tbl_product_media pm WHERE pm.product_id = p.id ORDER BY pm.position ASC LIMIT 1) AS mediaId,
            (SELECT MIN(pv.price) FROM tbl_product_variant pv WHERE pv.product_id = p.id) AS minPrice
        FROM tbl_product p
        LEFT JOIN tbl_category c ON p.category_id = c.id
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY 
            CASE WHEN LOWER(p.name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 0 ELSE 1 END,
            p.id DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<ProductSuggestionProjection> findSuggestions(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    @Query(value = """
        WITH product_attrs AS (
            SELECT 
                pav.product_id,
                pa.id AS attribute_id,
                pa.name AS attribute_name,
                pav.value AS attribute_value
            FROM tbl_product p
            JOIN tbl_product_attribute_value pav ON p.id = pav.product_id
            JOIN tbl_product_attribute pa ON pa.id = pav.product_attribute_id
            WHERE (:hasCategories = false OR p.category_id IN (:categoryIds))
              AND (:hasKeyword = false OR (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.meta_keyword) LIKE LOWER(CONCAT('%', :keyword, '%'))
              ))

            UNION

            SELECT 
                pv.product_id,
                pa.id AS attribute_id,
                pa.name AS attribute_name,
                pvav.value AS attribute_value
            FROM tbl_product p
            JOIN tbl_product_variant pv ON p.id = pv.product_id
            JOIN tbl_variant_attribute_value pvav ON pv.id = pvav.variant_id
            JOIN tbl_product_attribute pa ON pa.id = pvav.product_attribute_id
            WHERE (:hasCategories = false OR p.category_id IN (:categoryIds))
              AND (:hasKeyword = false OR (
                    LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.meta_keyword) LIKE LOWER(CONCAT('%', :keyword, '%'))
              ))
        )
        SELECT 
            attribute_id AS attributeId,
            attribute_name AS attributeName,
            attribute_value AS attributeValue,
            COUNT(DISTINCT product_id) AS productCount
        FROM product_attrs
        GROUP BY attribute_id, attribute_name, attribute_value
        ORDER BY attribute_name ASC, attribute_value ASC
    """, nativeQuery = true)
    List<AttributeFacetProjection> findAttributeFacets(
            @Param("keyword") String keyword,
            @Param("hasKeyword") boolean hasKeyword,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("hasCategories") boolean hasCategories
    );

    @Query(value = """
        SELECT 
            b.id AS brandId,
            b.name AS brandName,
            COUNT(DISTINCT p.id) AS productCount
        FROM tbl_product p
        JOIN tbl_brand b ON p.brand_id = b.id
        WHERE (:hasCategories = false OR p.category_id IN (:categoryIds))
          AND (:hasKeyword = false OR (
                LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.meta_keyword) LIKE LOWER(CONCAT('%', :keyword, '%'))
          ))
        GROUP BY b.id, b.name
        ORDER BY b.name ASC
    """, nativeQuery = true)
    List<BrandFacetProjection> findBrandFacets(
            @Param("keyword") String keyword,
            @Param("hasKeyword") boolean hasKeyword,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("hasCategories") boolean hasCategories
    );

    @Query(value = """
        SELECT 
            MIN(pv.price) AS minPrice,
            MAX(pv.price) AS maxPrice
        FROM tbl_product p
        JOIN tbl_product_variant pv ON p.id = pv.product_id
        WHERE (:hasCategories = false OR p.category_id IN (:categoryIds))
          AND (:hasKeyword = false OR (
                LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.meta_keyword) LIKE LOWER(CONCAT('%', :keyword, '%'))
          ))
    """, nativeQuery = true)
    PriceRangeProjection findPriceRange(
            @Param("keyword") String keyword,
            @Param("hasKeyword") boolean hasKeyword,
            @Param("categoryIds") List<Integer> categoryIds,
            @Param("hasCategories") boolean hasCategories
    );
}
