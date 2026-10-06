package com.yas.system.inventory.internal.repository;

import com.yas.system.inventory.internal.entity.StockReservation;
import com.yas.system.inventory.internal.enumeration.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderId(String orderId);

    List<StockReservation> findByOrderIdAndStatus(String orderId, ReservationStatus status);

    List<StockReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant now);
}
