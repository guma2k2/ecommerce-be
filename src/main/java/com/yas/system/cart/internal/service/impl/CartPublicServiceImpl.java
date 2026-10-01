package com.yas.system.cart.internal.service.impl;

import com.yas.system.cart.api.CartPublicService;
import com.yas.system.cart.api.dto.CartItemPublicDto;
import com.yas.system.cart.internal.entity.Cart;
import com.yas.system.cart.internal.repository.CartRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartPublicServiceImpl implements CartPublicService {

    CartRepository cartRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CartItemPublicDto> getCartItemsByCustomerId(String customerId) {
        if (customerId == null) {
            return List.of();
        }
        return cartRepository.findByCustomerId(customerId)
                .stream()
                .map(cart -> new CartItemPublicDto(cart.getId(), cart.getProductVariantId(), cart.getQuantity()))
                .toList();
    }

    @Override
    @Transactional
    public void clearCart(String customerId) {
        if (customerId != null) {
            cartRepository.deleteByCustomerId(customerId);
        }
    }
}
