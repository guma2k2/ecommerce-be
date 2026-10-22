package com.yas.system.promotion.internal.repository;

import com.yas.system.promotion.internal.entity.Promotion;
import com.yas.system.promotion.internal.enumeration.UsageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

    @Query("""
        select p
        from Promotion p
        left join fetch p.applies
        where lower(p.couponCode) = lower(:couponCode)
    """)
    Optional<Promotion> findByCouponCodeIgnoreCaseWithApplies(@Param("couponCode") String couponCode);

    @Query("""
        select p
        from Promotion p
        left join fetch p.applies
        where p.id = :id
    """)
    Optional<Promotion> findByIdWithApplies(@Param("id") Long id);

    Optional<Promotion> findBySlug(String slug);

    boolean existsByCouponCodeIgnoreCase(String couponCode);

    boolean existsByCouponCodeIgnoreCaseAndIdNot(String couponCode, Long id);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    @Modifying
    @Query("""
        update Promotion p
        set p.usageCount = p.usageCount + 1
        where p.id = :id
          and (p.usageType = :unlimited
               or p.usageCount < p.usageLimit)
    """)
    int incrementUsageCountAtomic(@Param("id") Long id, @Param("unlimited") UsageType unlimited);

    @Modifying
    @Query("""
        update Promotion p
        set p.usageCount = case when p.usageCount > 0 then p.usageCount - 1 else 0 end
        where p.id = :id
    """)
    int decrementUsageCountAtomic(@Param("id") Long id);
}
