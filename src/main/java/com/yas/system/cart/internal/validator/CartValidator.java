package com.yas.system.cart.internal.validator;

import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartValidator {

    public void validateCustomer(AuthUser customer) {
        if (customer == null || customer.id() == null) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public void validateStockAvailability(int requestedQuantity, ProductVariantPublicDto variant) {
        if (variant == null) {
            throw new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
        }
        int availableStock = variant.stockQuantity() != null ? variant.stockQuantity() : 0;
        if (requestedQuantity > availableStock) {
            throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK, requestedQuantity, availableStock);
        }
    }
}
