package com.yas.system.order.internal.repository;

import com.yas.system.order.internal.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderId(String orderId);

    List<OrderDetail> findByOrderIdIn(Collection<String> orderIds);
}
