package com.yas.system.order.internal.service;

import com.yas.system.common.response.PageResponse;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.order.internal.dto.request.CreateOrderRequest;
import com.yas.system.order.internal.dto.response.OrderCreateResponse;
import com.yas.system.order.internal.dto.response.OrderDetailResponse;
import com.yas.system.order.internal.dto.response.OrderSummaryResponse;
import com.yas.system.order.internal.enumeration.OrderStatus;

import java.util.UUID;

public interface OrderService {

    OrderCreateResponse createOrder(AuthUser customer, CreateOrderRequest request);

    PageResponse<OrderSummaryResponse> getCustomerOrders(AuthUser customer, int pageNumber, int pageSize, OrderStatus status);

    OrderDetailResponse getOrderDetail(AuthUser customer, UUID orderId);

    void cancelOrder(AuthUser customer, UUID orderId);
}
