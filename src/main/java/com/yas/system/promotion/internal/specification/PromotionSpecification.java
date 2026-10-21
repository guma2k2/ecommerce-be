package com.yas.system.promotion.internal.specification;

import com.yas.system.promotion.internal.entity.Promotion;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PromotionSpecification {

    private PromotionSpecification() {
    }

    public static Specification<Promotion> filter(
            String name,
            String couponCode,
            Boolean isActive,
            ZonedDateTime startDate,
            ZonedDateTime endDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(couponCode)) {
                predicates.add(cb.like(cb.lower(root.get("couponCode")), "%" + couponCode.trim().toLowerCase() + "%"));
            }

            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("endDate"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
