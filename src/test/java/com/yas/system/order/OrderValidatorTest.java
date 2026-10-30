package com.yas.system.order;

import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.order.internal.entity.Order;
import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.order.internal.validator.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderValidatorTest {

    private OrderValidator orderValidator;

    @BeforeEach
    void setUp() {
        orderValidator = new OrderValidator();
    }

    @Test
    @DisplayName("validateCustomer throws UNAUTHENTICATED if customer is null or id is null")
    void validateCustomer_unauthenticated() {
        assertThatThrownBy(() -> orderValidator.validateCustomer(null))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.UNAUTHENTICATED.getMessage());

        AuthUser userNoId = new AuthUser(null, "test@example.com", "USER", "pw", List.of());
        assertThatThrownBy(() -> orderValidator.validateCustomer(userNoId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.UNAUTHENTICATED.getMessage());
    }

    @Test
    @DisplayName("validateItemQuantities throws ORDER_ITEMS_EMPTY when empty")
    void validateItemQuantities_empty() {
        assertThatThrownBy(() -> orderValidator.validateItemQuantities(Collections.emptyMap()))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.ORDER_ITEMS_EMPTY.getMessage());
    }

    @Test
    @DisplayName("validateStockAvailability throws PRODUCT_VARIANT_NOT_FOUND when variant missing")
    void validateStockAvailability_missingVariant() {
        Map<Long, Integer> items = Map.of(1L, 2);
        Map<Long, ProductVariantPublicDto> variantMap = Map.of();

        assertThatThrownBy(() -> orderValidator.validateStockAvailability(items, variantMap))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_VARIANT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("validateStockAvailability throws INSUFFICIENT_STOCK when quantity exceeds available")
    void validateStockAvailability_insufficient() {
        Map<Long, Integer> items = Map.of(1L, 5);
        ProductVariantPublicDto variant = createVariantDto(1L, 2);
        Map<Long, ProductVariantPublicDto> variantMap = Map.of(1L, variant);

        assertThatThrownBy(() -> orderValidator.validateStockAvailability(items, variantMap))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("validateCancellation throws ORDER_CANNOT_BE_CANCELLED when status is not PENDING")
    void validateCancellation_notPending() {
        Order order = Order.builder()
                .status(OrderStatus.SHIPPING)
                .build();
        ReflectionTestUtils.setField(order, "createdAt", ZonedDateTime.now());

        assertThatThrownBy(() -> orderValidator.validateCancellation(order, UUID.randomUUID(), 30L))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("validateCancellation passes for recent PENDING order")
    void validateCancellation_success() {
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .build();
        ReflectionTestUtils.setField(order, "createdAt", ZonedDateTime.now().minusMinutes(5));

        assertThatCode(() -> orderValidator.validateCancellation(order, UUID.randomUUID(), 30L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCancellation throws ORDER_CANCELLATION_WINDOW_EXPIRED when expired")
    void validateCancellation_expired() {
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .build();
        ReflectionTestUtils.setField(order, "createdAt", ZonedDateTime.now().minusMinutes(35));

        assertThatThrownBy(() -> orderValidator.validateCancellation(order, UUID.randomUUID(), 30L))
                .isInstanceOf(ApplicationException.class);
    }

    private ProductVariantPublicDto createVariantDto(Long variantId, int stockQuantity) {
        return new ProductVariantPublicDto(
                variantId, 1L, "Product", "product", "thumb.jpg",
                stockQuantity, "SKU", BigDecimal.valueOf(100),
                List.of(), null, null
        );
    }
}
