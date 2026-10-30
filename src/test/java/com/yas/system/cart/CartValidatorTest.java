package com.yas.system.cart;

import com.yas.system.cart.internal.validator.CartValidator;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartValidatorTest {

    private CartValidator cartValidator;

    @BeforeEach
    void setUp() {
        cartValidator = new CartValidator();
    }

    @Test
    @DisplayName("validateCustomer throws UNAUTHENTICATED if customer is null or id is null")
    void validateCustomer_unauthenticated() {
        assertThatThrownBy(() -> cartValidator.validateCustomer(null))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.UNAUTHENTICATED.getMessage());

        AuthUser userNoId = new AuthUser(null, "user@test.com", "USER", "pw", List.of());
        assertThatThrownBy(() -> cartValidator.validateCustomer(userNoId))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.UNAUTHENTICATED.getMessage());
    }

    @Test
    @DisplayName("validateStockAvailability throws PRODUCT_VARIANT_NOT_FOUND when variant is null")
    void validateStockAvailability_nullVariant() {
        assertThatThrownBy(() -> cartValidator.validateStockAvailability(2, null))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_VARIANT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("validateStockAvailability throws INSUFFICIENT_STOCK when quantity exceeds stock")
    void validateStockAvailability_insufficient() {
        ProductVariantPublicDto variant = createVariantDto(1L, 3);

        assertThatThrownBy(() -> cartValidator.validateStockAvailability(5, variant))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    @DisplayName("validateStockAvailability succeeds when quantity <= stock")
    void validateStockAvailability_sufficient() {
        ProductVariantPublicDto variant = createVariantDto(1L, 10);

        assertThatCode(() -> cartValidator.validateStockAvailability(5, variant))
                .doesNotThrowAnyException();
    }

    private ProductVariantPublicDto createVariantDto(Long variantId, int stockQuantity) {
        return new ProductVariantPublicDto(
                variantId, 1L, "Product", "product", "thumb.jpg",
                stockQuantity, "SKU", BigDecimal.valueOf(100),
                List.of(), null, null
        );
    }
}
