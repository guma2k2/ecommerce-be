package com.yas.system.inventory.internal.repository;

import com.yas.system.inventory.internal.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    Optional<Inventory> findByProductVariantId(Long productVariantId);

    List<Inventory> findByProductVariantIdIn(Collection<Long> productVariantIds);

    Optional<Inventory> findBySku(String sku);

    /**
     * Atomically reserve stock: only increments `reserved` if available stock (onHand - reserved) >= quantity.
     * Returns number of updated rows (1 if successful, 0 if insufficient stock).
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reserved = i.reserved + :qty " +
           "WHERE i.productVariantId = :variantId AND (i.onHand - i.reserved) >= :qty")
    int reserveStock(@Param("variantId") Long variantId, @Param("qty") int qty);

    /**
     * Atomically release reserved stock: decrements `reserved`.
     * Returns number of updated rows.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.reserved = i.reserved - :qty " +
           "WHERE i.productVariantId = :variantId AND i.reserved >= :qty")
    int releaseReservation(@Param("variantId") Long variantId, @Param("qty") int qty);

    /**
     * Atomically confirm stock deduction: decrements both `onHand` and `reserved`.
     * Returns number of updated rows.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.onHand = i.onHand - :qty, i.reserved = i.reserved - :qty " +
           "WHERE i.productVariantId = :variantId AND i.reserved >= :qty AND i.onHand >= :qty")
    int confirmDeduction(@Param("variantId") Long variantId, @Param("qty") int qty);

    /**
     * Atomically adjust on-hand stock by delta.
     * Prevents onHand from dropping below currently active reserved stock.
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.onHand = i.onHand + :delta " +
           "WHERE i.productVariantId = :variantId AND (i.onHand + :delta) >= i.reserved")
    int adjustOnHand(@Param("variantId") Long variantId, @Param("delta") int delta);
}
