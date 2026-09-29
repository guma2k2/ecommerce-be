package com.yas.system.cart.controller;

import com.yas.system.cart.internal.dto.request.AddToCartRequest;
import com.yas.system.cart.internal.dto.request.UpdateCartQuantityRequest;
import com.yas.system.cart.internal.dto.response.CartResponse;
import com.yas.system.cart.internal.service.CartService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.annotation.ActiveUser;
import com.yas.system.common.security.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/carts")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CartController {

    CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@ActiveUser AuthUser customer) {
        return ApiResponse.success(cartService.getCart(customer));
    }

    @PostMapping
    public ApiResponse<CartResponse> addToCart(
            @ActiveUser AuthUser customer,
            @Valid @RequestBody AddToCartRequest request
    ) {
        return ApiResponse.success(cartService.addToCart(customer, request));
    }

    @PutMapping("/{cartId}")
    public ApiResponse<CartResponse> updateQuantity(
            @ActiveUser AuthUser customer,
            @PathVariable Long cartId,
            @Valid @RequestBody UpdateCartQuantityRequest request
    ) {
        return ApiResponse.success(cartService.updateQuantity(customer, cartId, request));
    }

    @DeleteMapping("/{cartId}")
    public ApiResponse<Void> deleteCartItem(
            @ActiveUser AuthUser customer,
            @PathVariable Long cartId
    ) {
        cartService.deleteCartItem(customer, cartId);
        return ApiResponse.successWithNoContent();
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(@ActiveUser AuthUser customer) {
        cartService.clearCart(customer);
        return ApiResponse.successWithNoContent();
    }
}
