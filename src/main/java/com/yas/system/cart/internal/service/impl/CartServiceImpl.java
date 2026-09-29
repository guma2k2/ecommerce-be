package com.yas.system.cart.internal.service.impl;

import com.yas.system.cart.internal.dto.request.AddToCartRequest;
import com.yas.system.cart.internal.dto.request.UpdateCartQuantityRequest;
import com.yas.system.cart.internal.dto.response.CartResponse;
import com.yas.system.cart.internal.entity.Cart;
import com.yas.system.cart.internal.helper.CartHelper;
import com.yas.system.cart.internal.repository.CartRepository;
import com.yas.system.cart.internal.service.CartService;
import com.yas.system.catalog.api.CatalogPublicService;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    CartRepository cartRepository;
    CartHelper cartHelper;
    CatalogPublicService catalogPublicService;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(AuthUser customer) {
        String customerId = resolveCustomerId(customer);
        List<Cart> cartList = cartRepository.findByCustomerId(customerId);
        if (cartList.isEmpty()) {
            return cartHelper.buildCartResponse(List.of(), Map.of());
        }

        List<Long> variantIds = cartList.stream()
                .map(Cart::getProductVariantId)
                .distinct()
                .toList();

        Map<Long, ProductVariantPublicDto> variantMap = catalogPublicService.getProductVariantsByIds(variantIds);
        return cartHelper.buildCartResponse(cartList, variantMap);
    }

    @Override
    @Transactional
    public CartResponse addToCart(AuthUser customer, AddToCartRequest request) {
        String customerId = resolveCustomerId(customer);

        ProductVariantPublicDto variant = catalogPublicService.getProductVariantById(request.productVariantId());
        if ("INACTIVE".equalsIgnoreCase(variant.status())) {
            throw new ApplicationException(ErrorCode.PRODUCT_VARIANT_INACTIVE);
        }

        Optional<Cart> existingCartOpt = cartRepository.findByCustomerIdAndProductVariantId(
                customerId,
                request.productVariantId()
        );

        int currentQuantity = existingCartOpt.map(Cart::getQuantity).orElse(0);
        int targetQuantity = currentQuantity + request.quantity();

        int availableStock = variant.stockQuantity() != null ? variant.stockQuantity() : 0;
        if (targetQuantity > availableStock) {
            throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK, targetQuantity, availableStock);
        }

        if (existingCartOpt.isPresent()) {
            Cart existingCart = existingCartOpt.get();
            existingCart.setQuantity(targetQuantity);
            cartRepository.save(existingCart);
        } else {
            Cart newCart = Cart.builder()
                    .customerId(customerId)
                    .productVariantId(request.productVariantId())
                    .quantity(targetQuantity)
                    .build();
            cartRepository.save(newCart);
        }

        return getCart(customer);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(AuthUser customer, Long cartId, UpdateCartQuantityRequest request) {
        String customerId = resolveCustomerId(customer);

        Cart cart = cartRepository.findByIdAndCustomerId(cartId, customerId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));

        ProductVariantPublicDto variant = catalogPublicService.getProductVariantById(cart.getProductVariantId());
        if ("INACTIVE".equalsIgnoreCase(variant.status())) {
            throw new ApplicationException(ErrorCode.PRODUCT_VARIANT_INACTIVE);
        }

        int availableStock = variant.stockQuantity() != null ? variant.stockQuantity() : 0;
        if (request.quantity() > availableStock) {
            throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK, request.quantity(), availableStock);
        }

        cart.setQuantity(request.quantity());
        cartRepository.save(cart);

        return getCart(customer);
    }

    @Override
    @Transactional
    public void deleteCartItem(AuthUser customer, Long cartId) {
        String customerId = resolveCustomerId(customer);
        Cart cart = cartRepository.findByIdAndCustomerId(cartId, customerId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND));
        cartRepository.delete(cart);
    }

    @Override
    @Transactional
    public void clearCart(AuthUser customer) {
        String customerId = resolveCustomerId(customer);
        cartRepository.deleteByCustomerId(customerId);
    }

    private String resolveCustomerId(AuthUser customer) {
        if (customer == null || customer.id() == null) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
        return customer.id();
    }
}
