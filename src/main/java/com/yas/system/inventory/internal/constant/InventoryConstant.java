package com.yas.system.inventory.internal.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InventoryConstant {

    public static final Duration DEFAULT_RESERVATION_TTL = Duration.ofMinutes(15);
    public static final String SYSTEM_USER = "SYSTEM";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ReasonDefaultNote {
        public static final String INITIAL_STOCK = "Initial stock creation";
        public static final String CYCLE_COUNT = "Physical stock cycle count";
        public static final String ORDER_DEDUCTION = "Order fulfillment deduction";
        public static final String ORDER_CANCELLATION = "Order cancellation stock restoration";
    }
}
