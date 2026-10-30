package com.yas.system.order.internal.validator;

import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.order.internal.entity.Order;
import com.yas.system.order.internal.enumeration.OrderStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderValidator {

    public void validateCustomer(AuthUser customer) {
        if (customer == null || customer.id() == null) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public void validateItemQuantities(Map<Long, Integer> itemQuantities) {
        if (itemQuantities == null || itemQuantities.isEmpty()) {
            throw new ApplicationException(ErrorCode.ORDER_ITEMS_EMPTY);
        }
    }

    public void validateStockAvailability(Map<Long, Integer> itemQuantities, Map<Long, ProductVariantPublicDto> variantMap) {
        for (Map.Entry<Long, Integer> entry : itemQuantities.entrySet()) {
            Long variantId = entry.getKey();
            int requestedQty = entry.getValue();

            ProductVariantPublicDto variant = variantMap.get(variantId);
            if (variant == null) {
                throw new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND);
            }
            int availableStock = variant.stockQuantity() != null ? variant.stockQuantity() : 0;
            if (requestedQty > availableStock) {
                throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK, requestedQty, availableStock);
            }
        }
    }

    public void validateCancellation(Order order, UUID orderId, Long windowMinutes) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ApplicationException(ErrorCode.ORDER_CANNOT_BE_CANCELLED, orderId, order.getStatus());
        }

        if (windowMinutes != null && windowMinutes > 0 && order.getCreatedAt() != null) {
            ZonedDateTime cutoff = order.getCreatedAt().plusMinutes(windowMinutes);
            if (ZonedDateTime.now().isAfter(cutoff)) {
                throw new ApplicationException(ErrorCode.ORDER_CANCELLATION_WINDOW_EXPIRED, orderId, windowMinutes);
            }
        }
    }
}

