package com.yas.system.inventory.internal.service;

import com.yas.system.inventory.api.dto.StockReservationItemDto;

import java.time.Duration;
import java.util.List;

public interface StockReservationService {

    void reserveStock(String orderId, List<StockReservationItemDto> items, Duration ttl);

    void confirmDeductions(String orderId);

    void releaseReservations(String orderId);

    void releaseExpiredReservations();
}
