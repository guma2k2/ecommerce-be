package com.yas.system.order.internal.helper;

import com.yas.system.common.util.DateTimeUtils;
import com.yas.system.order.internal.dto.request.CreateOrderRequest;
import com.yas.system.order.internal.dto.request.ShippingAddressRequest;
import com.yas.system.order.internal.dto.response.*;
import com.yas.system.order.internal.entity.Order;
import com.yas.system.order.internal.entity.OrderDetail;
import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
public class OrderHelper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generateOrderCode() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD-" + datePart + "-" + randomPart;
    }

    public Order buildOrder(
            String customerId,
            String orderCode,
            CreateOrderRequest request,
            BigDecimal totalAmount,
            BigDecimal shippingFee
    ) {
        ShippingAddressRequest address = request.shippingAddress();
        return Order.builder()
                .customerId(customerId)
                .orderCode(orderCode)
                .status(OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingFee(shippingFee)
                .receiverName(address.receiverName())
                .receiverPhone(address.receiverPhone())
                .shippingAddress(address.shippingAddress())
                .city(address.city())
                .district(address.district())
                .postalCode(address.postalCode())
                .note(request.note())
                .build();
    }

    public OrderDetailResponse toOrderDetailResponse(Order order, List<OrderDetail> details) {
        ShippingAddressResponse addressResponse = new ShippingAddressResponse(
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getShippingAddress(),
                order.getCity(),
                order.getDistrict(),
                order.getPostalCode()
        );

        List<OrderItemResponse> itemResponses = details.stream()
                .map(this::toOrderItemResponse)
                .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                order.getShippingFee(),
                addressResponse,
                order.getNote(),
                DateTimeUtils.format(order.getCreatedAt()),
                DateTimeUtils.format(order.getUpdatedAt()),
                itemResponses
        );
    }

    public OrderSummaryResponse toOrderSummaryResponse(Order order, int totalItems) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                totalItems,
                DateTimeUtils.format(order.getCreatedAt()),
                DateTimeUtils.format(order.getUpdatedAt())
        );
    }

    public OrderItemResponse toOrderItemResponse(OrderDetail detail) {
        return new OrderItemResponse(
                detail.getId(),
                detail.getProductVariantId(),
                detail.getProductName(),
                detail.getSku(),
                detail.getThumbnailUrl(),
                detail.getQuantity(),
                detail.getUnitPrice(),
                detail.getTotalPrice()
        );
    }
}
