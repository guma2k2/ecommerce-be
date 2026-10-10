package com.yas.system.order;

import com.yas.system.cart.api.CartPublicService;
import com.yas.system.cart.api.dto.CartItemPublicDto;
import com.yas.system.catalog.api.CatalogPublicService;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.catalog.api.enumeration.ProductStatus;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.response.PageResponse;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.inventory.api.InventoryPublicService;
import com.yas.system.order.internal.dto.request.CreateOrderRequest;
import com.yas.system.order.internal.dto.request.OrderItemRequest;
import com.yas.system.order.internal.dto.request.ShippingAddressRequest;
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
import com.yas.system.order.internal.service.impl.OrderServiceImpl;
import com.yas.system.payment.api.PaymentPublicService;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderDetailRepository orderDetailRepository;

    @Mock
    CatalogPublicService catalogPublicService;

    @Mock
    CartPublicService cartPublicService;

    @Mock
    PaymentPublicService paymentPublicService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    InventoryPublicService inventoryPublicService;

    OrderHelper orderHelper;
    OrderServiceImpl orderService;

    AuthUser testUser;
    final String customerId = "00000000-0000-0000-0000-000000000001";

    @BeforeEach
    void setUp() {
        orderHelper = new OrderHelper();
        orderService = new OrderServiceImpl(
                orderRepository,
                orderDetailRepository,
                orderHelper,
                catalogPublicService,
                cartPublicService,
                paymentPublicService,
                eventPublisher,
                inventoryPublicService
        );
        testUser = new AuthUser(customerId, "customer@example.com", "CUSTOMER", "password", List.of());
    }

    @Test
    @DisplayName("createOrder from explicit items should successfully create order and deduct stock")
    void createOrder_fromExplicitItems_success() {
        ShippingAddressRequest address = new ShippingAddressRequest(
                "John Doe", "0123456789", "123 Street", "Hanoi", "Ba Dinh", "10000"
        );
        CreateOrderRequest request = new CreateOrderRequest(
                address,
                PaymentMethod.COD,
                "Leave at doorstep",
                false,
                List.of(new OrderItemRequest(10L, 2))
        );

        ProductVariantPublicDto variantDto = createVariantDto(
                10L, 1L, "Test Phone", "test-phone", "thumb.jpg", 10, "SKU-001",
                BigDecimal.valueOf(100.00), "ACTIVE"
        );

        when(catalogPublicService.getProductVariantsByIds(any())).thenReturn(Map.of(10L, variantDto));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(UUID.randomUUID());
            }
            return o;
        });

        OrderCreateResponse response = orderService.createOrder(testUser, request);

        assertThat(response).isNotNull();
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.COD);
        assertThat(response.checkoutUrl()).isNull();

        verify(catalogPublicService).deductStock(Map.of(10L, 2));
        verify(orderDetailRepository).saveAll(anyList());
        verify(eventPublisher).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("createOrder with STRIPE payment should generate checkout URL")
    void createOrder_stripePayment_generatesCheckoutUrl() {
        ShippingAddressRequest address = new ShippingAddressRequest(
                "John Doe", "0123456789", "123 Street", "Hanoi", "Ba Dinh", "10000"
        );
        CreateOrderRequest request = new CreateOrderRequest(
                address,
                PaymentMethod.STRIPE,
                null,
                false,
                List.of(new OrderItemRequest(10L, 1))
        );

        ProductVariantPublicDto variantDto = createVariantDto(
                10L, 1L, "Test Phone", "test-phone", "thumb.jpg", 10, "SKU-001",
                BigDecimal.valueOf(150.00), "ACTIVE"
        );

        when(catalogPublicService.getProductVariantsByIds(any())).thenReturn(Map.of(10L, variantDto));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(UUID.randomUUID());
            }
            return o;
        });
        when(paymentPublicService.createCheckoutSessionUrl(eq(customerId), eq("customer@example.com"), any(), any(), eq("USD"), any()))
                .thenReturn("https://checkout.stripe.com/test-session");

        OrderCreateResponse response = orderService.createOrder(testUser, request);

        assertThat(response).isNotNull();
        assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.com/test-session");
    }

    @Test
    @DisplayName("createOrder from cart should use cart items and clear cart")
    void createOrder_fromCart_success() {
        ShippingAddressRequest address = new ShippingAddressRequest(
                "Jane Doe", "0987654321", "456 Avenue", "HCM", "District 1", "70000"
        );
        CreateOrderRequest request = new CreateOrderRequest(
                address,
                PaymentMethod.COD,
                null,
                true,
                List.of()
        );

        when(cartPublicService.getCartItemsByCustomerId(customerId))
                .thenReturn(List.of(new CartItemPublicDto(1L, 20L, 3)));

        ProductVariantPublicDto variantDto = createVariantDto(
                20L, 2L, "Test Laptop", "test-laptop", "thumb2.jpg", 5, "SKU-002",
                BigDecimal.valueOf(500.00), "ACTIVE"
        );

        when(catalogPublicService.getProductVariantsByIds(any())).thenReturn(Map.of(20L, variantDto));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            if (o.getId() == null) {
                o.setId(UUID.randomUUID());
            }
            return o;
        });

        OrderCreateResponse response = orderService.createOrder(testUser, request);

        assertThat(response).isNotNull();
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1500.00));
        verify(cartPublicService).clearCart(customerId);
    }

    @Test
    @DisplayName("createOrder should throw ORDER_ITEMS_EMPTY when no items are found")
    void createOrder_emptyItems_throwsException() {
        ShippingAddressRequest address = new ShippingAddressRequest(
                "Jane Doe", "0987654321", "456 Avenue", "HCM", "District 1", "70000"
        );
        CreateOrderRequest request = new CreateOrderRequest(address, PaymentMethod.COD, null, false, List.of());

        assertThatThrownBy(() -> orderService.createOrder(testUser, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_ITEMS_EMPTY);
    }

    @Test
    @DisplayName("createOrder should throw INSUFFICIENT_STOCK when requested quantity exceeds available stock")
    void createOrder_insufficientStock_throwsException() {
        ShippingAddressRequest address = new ShippingAddressRequest(
                "Jane Doe", "0987654321", "456 Avenue", "HCM", "District 1", "70000"
        );
        CreateOrderRequest request = new CreateOrderRequest(
                address, PaymentMethod.COD, null, false, List.of(new OrderItemRequest(10L, 10))
        );

        ProductVariantPublicDto variantDto = createVariantDto(
                10L, 1L, "Test Phone", "test-phone", "thumb.jpg", 3, "SKU-001",
                BigDecimal.valueOf(100.00), "ACTIVE"
        );

        when(catalogPublicService.getProductVariantsByIds(any())).thenReturn(Map.of(10L, variantDto));

        assertThatThrownBy(() -> orderService.createOrder(testUser, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_STOCK);
    }

    @Test
    @DisplayName("createOrder should throw PRODUCT_INACTIVE when variant product is inactive")
    void createOrder_inactiveVariant_throwsException() {
        ShippingAddressRequest address = new ShippingAddressRequest(
                "Jane Doe", "0987654321", "456 Avenue", "HCM", "District 1", "70000"
        );
        CreateOrderRequest request = new CreateOrderRequest(
                address, PaymentMethod.COD, null, false, List.of(new OrderItemRequest(10L, 1))
        );

        ProductVariantPublicDto variantDto = createVariantDto(
                10L, 1L, "Test Phone", "test-phone", "thumb.jpg", 10, "SKU-001",
                BigDecimal.valueOf(100.00), "INACTIVE"
        );

        when(catalogPublicService.getProductVariantsByIds(any())).thenReturn(Map.of(10L, variantDto));

        assertThatThrownBy(() -> orderService.createOrder(testUser, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_INACTIVE);
    }

    @Test
    @DisplayName("getCustomerOrders should return paginated list of customer orders")
    void getCustomerOrders_success() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .customerId(customerId)
                .orderCode("ORD-20261001-TEST1")
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(250.00))
                .build();
        order.setId(orderId);

        OrderDetail detail = OrderDetail.builder()
                .orderId(orderId.toString())
                .productVariantId(10L)
                .productName("Item 1")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(125.00))
                .totalPrice(BigDecimal.valueOf(250.00))
                .build();

        when(orderRepository.findByCustomerIdOrderByCreatedAtDesc(eq(customerId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(orderDetailRepository.findByOrderIdIn(anyList()))
                .thenReturn(List.of(detail));

        PageResponse<OrderSummaryResponse> page = orderService.getCustomerOrders(testUser, 0, 10, null);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().getFirst().orderCode()).isEqualTo("ORD-20261001-TEST1");
    }

    @Test
    @DisplayName("getOrderDetail should return complete order details when found")
    void getOrderDetail_success() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .customerId(customerId)
                .orderCode("ORD-20261001-TEST2")
                .status(OrderStatus.CONFIRMED)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.SUCCEEDED)
                .totalAmount(BigDecimal.valueOf(300.00))
                .receiverName("John")
                .receiverPhone("0123")
                .shippingAddress("123 Street")
                .build();
        order.setId(orderId);

        OrderDetail detail = OrderDetail.builder()
                .orderId(orderId.toString())
                .productVariantId(15L)
                .productName("Item 2")
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(100.00))
                .totalPrice(BigDecimal.valueOf(300.00))
                .build();

        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderId(any())).thenReturn(List.of(detail));

        OrderDetailResponse response = orderService.getOrderDetail(testUser, orderId);

        assertThat(response).isNotNull();
        assertThat(response.orderCode()).isEqualTo("ORD-20261001-TEST2");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("cancelOrder should cancel pending order and restore stock")
    void cancelOrder_pendingOrder_success() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .customerId(customerId)
                .orderCode("ORD-20261001-TEST3")
                .status(OrderStatus.PENDING)
                .build();
        order.setId(orderId);

        OrderDetail detail = OrderDetail.builder()
                .orderId(orderId.toString())
                .productVariantId(10L)
                .quantity(4)
                .build();

        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));
        when(orderDetailRepository.findByOrderId(any())).thenReturn(List.of(detail));

        orderService.cancelOrder(testUser, orderId);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(catalogPublicService).restoreStock(Map.of(10L, 4));
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("cancelOrder should throw ORDER_CANNOT_BE_CANCELLED when status is not PENDING")
    void cancelOrder_shippedOrder_throwsException() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .customerId(customerId)
                .orderCode("ORD-20261001-TEST4")
                .status(OrderStatus.SHIPPING)
                .build();
        order.setId(orderId);

        when(orderRepository.findByIdAndCustomerId(orderId, customerId)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(testUser, orderId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORDER_CANNOT_BE_CANCELLED);
    }

    private ProductVariantPublicDto createVariantDto(Long variantId, Long productId, String name, String slug,
                                                    String thumbnail, int stockQuantity, String sku,
                                                    BigDecimal price, String status) {
        return new ProductVariantPublicDto(
                variantId, productId, name, slug, thumbnail,
                stockQuantity, sku, price, status,
                List.of(), null, null
        );
    }
}
