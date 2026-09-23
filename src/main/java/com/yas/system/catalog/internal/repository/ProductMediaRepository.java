package com.yas.system.catalog.internal.repository;

import com.yas.system.catalog.internal.entity.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMediaRepository extends JpaRepository<ProductMedia, Long> {

    List<ProductMedia> findByProductIdOrderByPositionAsc(Long productId);

    List<ProductMedia> findByProductIdInOrderByPositionAsc(List<Long> productIds);

    void deleteByProductId(Long productId);
}

