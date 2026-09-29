package com.yas.system.cart.internal.service;

import com.yas.system.cart.internal.dto.request.AddToCartRequest;
import com.yas.system.cart.internal.dto.request.UpdateCartQuantityRequest;
import com.yas.system.cart.internal.dto.response.CartResponse;
import com.yas.system.common.security.annotation.AuthUser;

public interface CartService {

    CartResponse getCart(AuthUser customer);

    CartResponse addToCart(AuthUser customer, AddToCartRequest request);

    CartResponse updateQuantity(AuthUser customer, Long cartId, UpdateCartQuantityRequest request);

    void deleteCartItem(AuthUser customer, Long cartId);

    void clearCart(AuthUser customer);
}
