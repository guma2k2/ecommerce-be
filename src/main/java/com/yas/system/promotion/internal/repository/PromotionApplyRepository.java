package com.yas.system.promotion.internal.repository;

import com.yas.system.promotion.internal.entity.PromotionApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionApplyRepository extends JpaRepository<PromotionApply, Long> {

    List<PromotionApply> findByPromotionId(Long promotionId);

    void deleteByPromotionId(Long promotionId);
}
