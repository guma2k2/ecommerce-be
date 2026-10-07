package com.yas.system.inventory.internal.service.impl;

import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.internal.dto.response.StockMovementResponse;
import com.yas.system.inventory.internal.entity.StockMovement;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;
import com.yas.system.inventory.internal.repository.StockMovementRepository;
import com.yas.system.inventory.internal.service.StockMovementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockMovementServiceImpl implements StockMovementService {

    StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getMovementsByVariantId(Long variantId, int pageNumber, int pageSize) {
        return getMovementsByVariantId(variantId, PageRequest.of(pageNumber, pageSize));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> getMovementsByVariantId(Long variantId, Pageable pageable) {
        Page<StockMovement> page = stockMovementRepository.findByProductVariantIdOrderByCreatedAtDesc(variantId, pageable);
        List<StockMovementResponse> content = page.getContent().stream()
                .map(StockMovementResponse::from)
                .toList();

        return new PageResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional
    public StockMovement recordMovement(
            Long inventoryId,
            Long productVariantId,
            int quantityChange,
            int quantityBefore,
            int quantityAfter,
            MovementType movementType,
            MovementReason reason,
            String referenceId,
            String note,
            String performedBy
    ) {
        StockMovement movement = StockMovement.builder()
                .inventoryId(inventoryId)
                .productVariantId(productVariantId)
                .quantityChange(quantityChange)
                .quantityBefore(quantityBefore)
                .quantityAfter(quantityAfter)
                .movementType(movementType)
                .reason(reason)
                .referenceId(referenceId)
                .note(note)
                .performedBy(performedBy)
                .build();

        StockMovement saved = stockMovementRepository.save(movement);
        log.info("Recorded stock movement ID {} for variant {}: delta={}, before={}, after={}, reason={}",
                saved.getId(), productVariantId, quantityChange, quantityBefore, quantityAfter, reason);
        return saved;
    }
}
