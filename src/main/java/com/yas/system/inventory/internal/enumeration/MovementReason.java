package com.yas.system.inventory.internal.enumeration;

public enum MovementReason {
    INITIAL_STOCK,
    RESTOCK,
    CORRECTION,
    DAMAGED,
    THEFT_OR_LOSS,
    PROMOTION,
    CYCLE_COUNT_RECONCILIATION,
    ORDER_CHECKOUT,
    ORDER_CANCELLED,
    RETURN_RESTOCK
}
