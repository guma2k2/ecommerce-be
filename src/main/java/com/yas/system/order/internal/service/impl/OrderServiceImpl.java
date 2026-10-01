package com.yas.system.order.internal.service.impl;

import com.yas.system.cart.api.CartPublicService;
import com.yas.system.cart.api.dto.CartItemPublicDto;
import com.yas.system.catalog.api.CatalogPublicService;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.response.PageResponse;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.order.internal.dto.request.CreateOrderRequest;
import com.yas.system.order.internal.dto.request.OrderItemRequest;
import com.yas.system.order.internal.dto.response.OrderCreateResponse;
import com.yas.system.order.internal.dto.response.OrderDetailResponse;
import com.yas.system.order.internal.dto.response.OrderSummaryResponse;
import com.yas.system.order.internal.entity.Order;
import com.yas.system.order.internal.entity.OrderDetail;
import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.order.internal.event.OrderCreatedEvent;
import com.yas.system.order.internal.helper.OrderHelper;
import com.yas.system.order.internal.repository.OrderDetailRepository;
import com.yas.system.order.internal.repository.OrderRepository;
import com.yas.system.order.internal.service.OrderService;
import com.yas.system.payment.api.PaymentPublicService;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderDetailRepository orderDetailRepository;
    OrderHelper orderHelper;
    CatalogPublicService catalogPublicService;
    CartPublicService cartPublicService;
    PaymentPublicService paymentPublicService;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderCreateResponse createOrder(AuthUser customer, CreateOrderRequest request) {
        String customerId = resolveCustomerId(customer);

        // 1. Resolve order items and quantities
        Map<Long, Integer> itemQuantities = resolveItemQuantities(customerId, request);
        if (itemQuantities.isEmpty()) {
            throw new ApplicationException(ErrorCode.ORDER_ITEMS_EMPTY);
        }

        // 2. Fetch variants and validate status & stock
        Map<Long, ProductVariantPublicDto> variantMap = catalogPublicService.getProductVariantsByIds(itemQuantities.keySet());
        for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
            Long variantId = entry.getKey();
            int requestedQty = entry.getValue();

            ProductVariantPublicDto variant = variantMap.get(variantId);
            if (variant == null) {
                throw new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
            }
            if ("INACTIVE".equalsIgnoreCase(variant.status())) {
                throw new ApplicationException(ErrorCode.PRODUCT_VARIANT_INACTIVE);
            }
            int availableStock = variant.stockQuantity() != null ? variant.stockQuantity() : 0;
            if (requestedQty > availableStock) {
                throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK, requestedQty, availableStock);
            }
        }

        // 3. Deduct stock atomically
        catalogPublicService.deductStock(itemQuantities);

        // 4. Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
            ProductVariantPublicDto variant = variantMap.get(entry.getKey());
            int qty = entry.getValue();
            BigDecimal unitPrice = variant.price() != null ? variant.price() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            totalAmount = totalAmount.add(lineTotal);
        }

        // 5. Build and save Order entity
        BigDecimal shippingFee = BigDecimal.ZERO;
        String orderCode = orderHelper.generateOrderCode();
        Order order = orderHelper.buildOrder(customerId, orderCode, request, totalAmount.add(shippingFee), shippingFee);
        Order savedOrder = orderRepository.save(order);

        // 6. Build and save OrderDetail entities
        String orderIdStr = savedOrder.getId().toString();
        for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
            ProductVariantPublicDto variant = variantMap.get(entry.getKey());
            int qty = entry.getValue();
            BigDecimal unitPrice = variant.price() != null ? variant.price() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderDetail detail = OrderDetail.builder()
                    .orderId(orderIdStr)
                    .productVariantId(variant.variantId())
                    .productName(variant.productName() != null ? variant.productName() : "Product")
                    .sku(variant.sku())
                    .thumbnailUrl(variant.thumbnailUrl())
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .totalPrice(lineTotal)
                    .build();
            details.add(detail);
        }
        orderDetailRepository.saveAll(details);

        // 7. Clear cart if order placed from cart
        if (request.fromCart()) {
            cartPublicService.clearCart(customerId);
        }

        // 8. Publish domain event
        List<Long> variantIds = new ArrayList<>(itemQuantities.keySet());
        eventPublisher.publishEvent(new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                customerId,
                savedOrder.getTotalAmount(),
                variantIds
        ));

        // 9. Generate payment checkout URL if STRIPE
        String checkoutUrl = null;
        if (request.paymentMethod() == PaymentMethod.STRIPE) {
            checkoutUrl = paymentPublicService.createCheckoutSessionUrl(
                    customerId,
                    customer.email(),
                    orderIdStr,
                    savedOrder.getTotalAmount(),
                    "USD"
            );
        }

        log.info("Order {} created successfully with code {} for customer {}", savedOrder.getId(), orderCode, customerId);

        return new OrderCreateResponse(
                savedOrder.getId(),
                savedOrder.getOrderCode(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                savedOrder.getPaymentMethod(),
                checkoutUrl
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getCustomerOrders(AuthUser customer, int pageNumber, int pageSize, OrderStatus status) {
        String customerId = resolveCustomerId(customer);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Order> orderPage = (status != null)
                ? orderRepository.findByCustomerIdAndStatusOrderByCreatedAtDesc(customerId, status, pageable)
                : orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable);

        if (orderPage.isEmpty()) {
            return new PageResponse<>(pageNumber, pageSize, 0, 0, List.of());
        }

        List<String> orderIdStrings = orderPage.getContent().stream()
                .map(o -> o.getId().toString())
                .toList();

        List<OrderDetail> allDetails = orderDetailRepository.findByOrderIdIn(orderIdStrings);
        Map<String, Integer> itemCountMap = allDetails.stream()
                .collect(Collectors.groupingBy(
                        OrderDetail::getOrderId,
                        Collectors.summingInt(OrderDetail::getQuantity)
                ));

        List<OrderSummaryResponse> content = orderPage.getContent().stream()
                .map(o -> orderHelper.toOrderSummaryResponse(o, itemCountMap.getOrDefault(o.getId().toString(), 0)))
                .toList();

        return new PageResponse<>(
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalPages(),
                orderPage.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(AuthUser customer, UUID orderId) {
        String customerId = resolveCustomerId(customer);
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND, orderId));

        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId().toString());
        return orderHelper.toOrderDetailResponse(order, details);
    }

    @Override
    @Transactional
    public void cancelOrder(AuthUser customer, UUID orderId) {
        String customerId = resolveCustomerId(customer);
        Order order = orderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.ORDER_NOT_FOUND, orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ApplicationException(ErrorCode.ORDER_CANNOT_BE_CANCELLED, orderId, order.getStatus());
        }

        // Restore stock
        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId().toString());
        Map<Long, Integer> restoreQuantities = details.stream()
                .collect(Collectors.toMap(OrderDetail::getProductVariantId, OrderDetail::getQuantity));
        catalogPublicService.restoreStock(restoreQuantities);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        log.info("Order {} cancelled by customer {}", orderId, customerId);
    }

    private Map<Long, Integer> resolveItemQuantities(String customerId, CreateOrderRequest request) {
        Map<Long, Integer> itemQuantities = new HashMap<>();

        if (request.fromCart()) {
            List<CartItemPublicDto> cartItems = cartPublicService.getCartItemsByCustomerId(customerId);
            for (CartItemPublicDto item : cartItems) {
                itemQuantities.merge(item.productVariantId(), item.quantity(), Integer::sum);
            }
        } else if (request.items() != null) {
            for (OrderItemRequest item : request.items()) {
                itemQuantities.merge(item.productVariantId(), item.quantity(), Integer::sum);
            }
        }

        return itemQuantities;
    }

    private String resolveCustomerId(AuthUser customer) {
        if (customer == null || customer.id() == null) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
        return customer.id();
    }
}
