package com.yas.system.order.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import com.yas.system.common.security.annotation.ActiveUser;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.order.internal.dto.request.CreateOrderRequest;
import com.yas.system.order.internal.dto.response.OrderCreateResponse;
import com.yas.system.order.internal.dto.response.OrderDetailResponse;
import com.yas.system.order.internal.dto.response.OrderSummaryResponse;
import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.order.internal.service.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @PostMapping
    public ApiResponse<OrderCreateResponse> createOrder(
            @ActiveUser AuthUser customer,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ApiResponse.success(orderService.createOrder(customer, request));
    }

    @GetMapping
    public ApiResponse<PageResponse<OrderSummaryResponse>> getCustomerOrders(
            @ActiveUser AuthUser customer,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) OrderStatus status
    ) {
        return ApiResponse.success(orderService.getCustomerOrders(customer, pageNumber, pageSize, status));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @ActiveUser AuthUser customer,
            @PathVariable UUID orderId
    ) {
        return ApiResponse.success(orderService.getOrderDetail(customer, orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(
            @ActiveUser AuthUser customer,
            @PathVariable UUID orderId
    ) {
        orderService.cancelOrder(customer, orderId);
        return ApiResponse.successWithNoContent();
    }
}
