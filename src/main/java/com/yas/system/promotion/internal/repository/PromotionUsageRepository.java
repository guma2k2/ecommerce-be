package com.yas.system.promotion.internal.repository;

import com.yas.system.promotion.internal.entity.PromotionUsage;
import com.yas.system.promotion.internal.enumeration.PromotionUsageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

    boolean existsByPromotionId(Long promotionId);

    Optional<PromotionUsage> findByOrderId(UUID orderId);

    Optional<PromotionUsage> findByOrderIdAndStatus(UUID orderId, PromotionUsageStatus status);

    long countByPromotionIdAndCustomerIdAndStatus(Long promotionId, String customerId, PromotionUsageStatus status);
}
