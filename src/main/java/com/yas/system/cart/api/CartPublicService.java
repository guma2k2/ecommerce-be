package com.yas.system.cart.api;

import com.yas.system.cart.api.dto.CartItemPublicDto;

import java.util.List;

public interface CartPublicService {

    List<CartItemPublicDto> getCartItemsByCustomerId(String customerId);

    void clearCart(String customerId);
}
