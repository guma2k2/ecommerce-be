package com.yas.system.cart;

import com.yas.system.cart.internal.dto.request.AddToCartRequest;
import com.yas.system.cart.internal.dto.request.UpdateCartQuantityRequest;
import com.yas.system.cart.internal.dto.response.CartResponse;
import com.yas.system.cart.internal.entity.Cart;
import com.yas.system.cart.internal.helper.CartHelper;
import com.yas.system.cart.internal.repository.CartRepository;
import com.yas.system.cart.internal.service.impl.CartServiceImpl;
import com.yas.system.catalog.api.CatalogPublicService;
import com.yas.system.catalog.api.dto.ProductOptionPublicDto;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.catalog.api.enumeration.ProductStatus;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    CatalogPublicService catalogPublicService;

    CartHelper cartHelper;
    CartServiceImpl cartService;

    AuthUser testUser;
    final String customerId = "00000000-0000-0000-0000-000000000001";

    @BeforeEach
    void setUp() {
        cartHelper = new CartHelper();
        cartService = new CartServiceImpl(cartRepository, cartHelper, catalogPublicService);
        testUser = new AuthUser(customerId, "test@example.com", "CUSTOMER", "password", List.of());
    }

    @Test
    @DisplayName("getCart should return empty cart when no items exist")
    void getCart_empty() {
        when(cartRepository.findByCustomerId(customerId)).thenReturn(List.of());

        CartResponse response = cartService.getCart(testUser);

        assertThat(response).isNotNull();
        assertThat(response.items()).isEmpty();
        assertThat(response.totalQuantity()).isZero();
        assertThat(response.totalPrice()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getCart should return items with calculated subtotal and totals")
    void getCart_withItems() {
        Cart cart = Cart.builder()
                .customerId(customerId)
                .productVariantId(100L)
                .quantity(2)
                .build();
        cart.setId(1L);

        ProductVariantPublicDto variantDto = createVariantDto(100L, 10, "ACTIVE",
                List.of(new ProductOptionPublicDto(1L, "Color", "Black")));

        when(cartRepository.findByCustomerId(customerId)).thenReturn(List.of(cart));
        when(catalogPublicService.getProductVariantsByIds(List.of(100L))).thenReturn(Map.of(100L, variantDto));

        CartResponse response = cartService.getCart(testUser);

        assertThat(response.items()).hasSize(1);
        assertThat(response.totalQuantity()).isEqualTo(2);
        assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(1998));
        assertThat(response.items().getFirst().subtotal()).isEqualByComparingTo(BigDecimal.valueOf(1998));
        assertThat(response.items().getFirst().variant().productName()).isEqualTo("iPhone 16");
        assertThat(response.items().getFirst().variant().options()).hasSize(1);
        assertThat(response.items().getFirst().variant().options().getFirst().name()).isEqualTo("Color");
        assertThat(response.items().getFirst().variant().options().getFirst().value()).isEqualTo("Black");
    }

    @Test
    @DisplayName("addToCart should create new item when variant not in cart")
    void addToCart_newItem() {
        ProductVariantPublicDto variantDto = createVariantDto(100L, 10, "ACTIVE", List.of());

        when(catalogPublicService.getProductVariantById(100L)).thenReturn(variantDto);
        when(cartRepository.findByCustomerIdAndProductVariantId(customerId, 100L)).thenReturn(Optional.empty());

        Cart cart = Cart.builder().customerId(customerId).productVariantId(100L).quantity(2).build();
        cart.setId(1L);
        when(cartRepository.findByCustomerId(customerId)).thenReturn(List.of(cart));
        when(catalogPublicService.getProductVariantsByIds(List.of(100L))).thenReturn(Map.of(100L, variantDto));

        AddToCartRequest request = new AddToCartRequest(100L, 2);
        CartResponse response = cartService.addToCart(testUser, request);

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantity()).isEqualTo(2);
        assertThat(captor.getValue().getProductVariantId()).isEqualTo(100L);
        assertThat(response.totalQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("addToCart should increment quantity when variant already in cart")
    void addToCart_existingItem() {
        ProductVariantPublicDto variantDto = createVariantDto(100L, 10, "ACTIVE", List.of());

        Cart existingCart = Cart.builder()
                .customerId(customerId)
                .productVariantId(100L)
                .quantity(3)
                .build();
        existingCart.setId(1L);

        when(catalogPublicService.getProductVariantById(100L)).thenReturn(variantDto);
        when(cartRepository.findByCustomerIdAndProductVariantId(customerId, 100L)).thenReturn(Optional.of(existingCart));

        Cart cartUpdated = Cart.builder().customerId(customerId).productVariantId(100L).quantity(5).build();
        cartUpdated.setId(1L);
        when(cartRepository.findByCustomerId(customerId)).thenReturn(List.of(cartUpdated));
        when(catalogPublicService.getProductVariantsByIds(List.of(100L))).thenReturn(Map.of(100L, variantDto));

        AddToCartRequest request = new AddToCartRequest(100L, 2);
        CartResponse response = cartService.addToCart(testUser, request);

        verify(cartRepository).save(existingCart);
        assertThat(existingCart.getQuantity()).isEqualTo(5);
        assertThat(response.totalQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("addToCart should throw INSUFFICIENT_STOCK when quantity exceeds available stock")
    void addToCart_insufficientStock() {
        ProductVariantPublicDto variantDto = createVariantDto(100L, 3, "ACTIVE", List.of());

        when(catalogPublicService.getProductVariantById(100L)).thenReturn(variantDto);
        when(cartRepository.findByCustomerIdAndProductVariantId(customerId, 100L)).thenReturn(Optional.empty());

        AddToCartRequest request = new AddToCartRequest(100L, 5);

        assertThatThrownBy(() -> cartService.addToCart(testUser, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_STOCK);

        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("addToCart should throw PRODUCT_INACTIVE when variant product status is INACTIVE")
    void addToCart_inactiveVariant() {
        ProductVariantPublicDto variantDto = createVariantDto(100L, 10, "INACTIVE", List.of());

        when(catalogPublicService.getProductVariantById(100L)).thenReturn(variantDto);

        AddToCartRequest request = new AddToCartRequest(100L, 1);

        assertThatThrownBy(() -> cartService.addToCart(testUser, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_INACTIVE);

        verify(cartRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateQuantity should successfully update quantity")
    void updateQuantity_success() {
        Cart cart = Cart.builder()
                .customerId(customerId)
                .productVariantId(100L)
                .quantity(1)
                .build();
        cart.setId(5L);

        ProductVariantPublicDto variantDto = createVariantDto(100L, 10, "ACTIVE", List.of());

        when(cartRepository.findByIdAndCustomerId(5L, customerId)).thenReturn(Optional.of(cart));
        when(catalogPublicService.getProductVariantById(100L)).thenReturn(variantDto);

        Cart cartUpdated = Cart.builder().customerId(customerId).productVariantId(100L).quantity(4).build();
        cartUpdated.setId(5L);
        when(cartRepository.findByCustomerId(customerId)).thenReturn(List.of(cartUpdated));
        when(catalogPublicService.getProductVariantsByIds(List.of(100L))).thenReturn(Map.of(100L, variantDto));

        UpdateCartQuantityRequest request = new UpdateCartQuantityRequest(4);
        CartResponse response = cartService.updateQuantity(testUser, 5L, request);

        assertThat(cart.getQuantity()).isEqualTo(4);
        verify(cartRepository).save(cart);
        assertThat(response.totalQuantity()).isEqualTo(4);
    }

    @Test
    @DisplayName("updateQuantity should throw CART_ITEM_NOT_FOUND when item doesn't exist")
    void updateQuantity_notFound() {
        when(cartRepository.findByIdAndCustomerId(99L, customerId)).thenReturn(Optional.empty());

        UpdateCartQuantityRequest request = new UpdateCartQuantityRequest(2);

        assertThatThrownBy(() -> cartService.updateQuantity(testUser, 99L, request))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("deleteCartItem should delete existing item")
    void deleteCartItem_success() {
        Cart cart = Cart.builder()
                .customerId(customerId)
                .productVariantId(100L)
                .quantity(1)
                .build();
        cart.setId(5L);

        when(cartRepository.findByIdAndCustomerId(5L, customerId)).thenReturn(Optional.of(cart));

        cartService.deleteCartItem(testUser, 5L);

        verify(cartRepository).delete(cart);
    }

    @Test
    @DisplayName("clearCart should delete all items for customer")
    void clearCart_success() {
        cartService.clearCart(testUser);

        verify(cartRepository).deleteByCustomerId(customerId);
    }

    private ProductVariantPublicDto createVariantDto(Long variantId, int stockQuantity, String status, List<ProductOptionPublicDto> options) {
        return new ProductVariantPublicDto(
                variantId, 10L, "iPhone 16", "iphone-16", "https://img.url",
                stockQuantity, "IPHONE-16-BLK", BigDecimal.valueOf(999), status,
                options, null, null
        );
    }
}
