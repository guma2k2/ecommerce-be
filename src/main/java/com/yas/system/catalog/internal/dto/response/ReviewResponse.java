package com.yas.system.catalog.internal.dto.response;

import com.tiki.product.entity.Review;

public record ReviewResponse(
        Long id,
        String content,
        int rating,
        CustomerResponse customer,
        String variant,
        String created_at,
        String updated_at
) {
    public static ReviewResponse fromModel(Review review,
                                           CustomerResponse customer,
                                           String variant
    ) {
        String updatedAt = review.getUpdatedAt() != null ? review.getUpdatedAt().toString() : "";
        return new ReviewResponse(
                review.getId(), review.getContent(), review.getRatingStar(), customer, variant, review.getCreatedAt().toString(), updatedAt
        );
    }
}
