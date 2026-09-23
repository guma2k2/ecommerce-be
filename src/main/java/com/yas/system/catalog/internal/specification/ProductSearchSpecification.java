package com.yas.system.catalog.internal.specification;

import com.yas.system.catalog.internal.dto.request.ProductSearchRequest;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeValue;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductSearchSpecification {

    public static Specification<Product> buildSpecification(
            ProductSearchRequest request,
            List<Integer> categoryIds
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Category filter (including descendant subcategories)
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            // 2. Keyword filter across name, slug, description, metaKeyword
            if (request.keyword() != null && !request.keyword().isBlank()) {
                String pattern = "%" + request.keyword().toLowerCase().trim() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate slugMatch = cb.like(cb.lower(root.get("slug")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate metaKeywordMatch = cb.like(cb.lower(root.get("metaKeyword")), pattern);
                predicates.add(cb.or(nameMatch, slugMatch, descMatch, metaKeywordMatch));
            }

            // 3. Brand filter
            if (request.brandIds() != null && !request.brandIds().isEmpty()) {
                predicates.add(root.get("brand").get("id").in(request.brandIds()));
            }

            // 4. Price range filter across product variants
            BigDecimal minPrice = request.minPrice();
            BigDecimal maxPrice = request.maxPrice();
            if (minPrice != null || maxPrice != null) {
                Subquery<Long> priceSubquery = query.subquery(Long.class);
                Root<ProductVariant> pvRoot = priceSubquery.from(ProductVariant.class);
                priceSubquery.select(pvRoot.get("product").get("id"));

                List<Predicate> pricePredicates = new ArrayList<>();
                pricePredicates.add(cb.equal(pvRoot.get("product"), root));
                if (minPrice != null) {
                    pricePredicates.add(cb.greaterThanOrEqualTo(pvRoot.get("price"), minPrice));
                }
                if (maxPrice != null) {
                    pricePredicates.add(cb.lessThanOrEqualTo(pvRoot.get("price"), maxPrice));
                }

                priceSubquery.where(pricePredicates.toArray(new Predicate[0]));
                predicates.add(cb.exists(priceSubquery));
            }

            // 5. Dynamic attribute filters (OR within same attribute, AND between different attributes)
            // Supports both ProductAttributeValue and ProductVariantAttributeValue
            Map<Long, List<String>> attributeFilters = request.attributeFilters();
            if (attributeFilters != null && !attributeFilters.isEmpty()) {
                for (Map.Entry<Long, List<String>> entry : attributeFilters.entrySet()) {
                    Long attributeId = entry.getKey();
                    List<String> values = entry.getValue();

                    if (attributeId == null || values == null || values.isEmpty()) {
                        continue;
                    }

                    // Product-level attribute match subquery
                    Subquery<Long> pavSubquery = query.subquery(Long.class);
                    Root<ProductAttributeValue> pavRoot = pavSubquery.from(ProductAttributeValue.class);
                    pavSubquery.select(pavRoot.get("product").get("id"));
                    pavSubquery.where(
                            cb.equal(pavRoot.get("product"), root),
                            cb.equal(pavRoot.get("productAttribute").get("id"), attributeId),
                            pavRoot.get("value").in(values)
                    );
                    Predicate productLevelMatch = cb.exists(pavSubquery);

                    // Variant-level attribute match subquery
                    Subquery<Long> pvavSubquery = query.subquery(Long.class);
                    Root<ProductVariantAttributeValue> pvavRoot = pvavSubquery.from(ProductVariantAttributeValue.class);
                    pvavSubquery.select(pvavRoot.get("productVariant").get("product").get("id"));
                    pvavSubquery.where(
                            cb.equal(pvavRoot.get("productVariant").get("product"), root),
                            cb.equal(pvavRoot.get("productAttribute").get("id"), attributeId),
                            pvavRoot.get("value").in(values)
                    );
                    Predicate variantLevelMatch = cb.exists(pvavSubquery);

                    // Match if either product-level or variant-level contains one of the values
                    predicates.add(cb.or(productLevelMatch, variantLevelMatch));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
