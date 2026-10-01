package com.yas.system.cart.internal.helper;

import com.yas.system.cart.internal.dto.response.CartItemResponse;
import com.yas.system.cart.internal.dto.response.CartResponse;
import com.yas.system.cart.internal.dto.response.ProductOptionResponse;
import com.yas.system.cart.internal.dto.response.ProductVariantResponse;
import com.yas.system.cart.internal.entity.Cart;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CartHelper {

    public CartResponse buildCartResponse(List<Cart> cartList, Map<Long, ProductVariantPublicDto> variantMap) {
        if (cartList == null || cartList.isEmpty()) {
            return new CartResponse(List.of(), 0, BigDecimal.ZERO);
        }

        List<CartItemResponse> items = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Cart cart : cartList) {
            ProductVariantPublicDto variantDto = variantMap.get(cart.getProductVariantId());
            if (variantDto != null) {
                List<ProductOptionResponse> optionResponses = variantDto.options() != null
                        ? variantDto.options().stream()
                                .map(opt -> new ProductOptionResponse(opt.id(), opt.name(), opt.value()))
                                .toList()
                        : List.of();

                ProductVariantResponse variantResponse = new ProductVariantResponse(
                        variantDto.variantId(),
                        variantDto.productId(),
                        variantDto.productName(),
                        variantDto.productSlug(),
                        variantDto.thumbnailUrl(),
                        variantDto.stockQuantity() != null ? variantDto.stockQuantity() : 0,
                        variantDto.sku(),
                        variantDto.price(),
                        optionResponses
                );

                BigDecimal price = variantDto.price() != null ? variantDto.price() : BigDecimal.ZERO;
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(cart.getQuantity()));

                totalQuantity += cart.getQuantity();
                totalPrice = totalPrice.add(subtotal);

                items.add(new CartItemResponse(
                        cart.getId(),
                        variantResponse,
                        cart.getQuantity(),
                        subtotal
                ));
            }
        }

        return new CartResponse(items, totalQuantity, totalPrice);
    }
}
