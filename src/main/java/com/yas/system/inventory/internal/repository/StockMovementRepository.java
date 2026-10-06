package com.yas.system.inventory.internal.repository;

import com.yas.system.inventory.internal.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long>, JpaSpecificationExecutor<StockMovement> {

    Page<StockMovement> findByProductVariantIdOrderByCreatedAtDesc(Long productVariantId, Pageable pageable);

    List<StockMovement> findByReferenceId(String referenceId);
}
