package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.internal.VariantUpdateContext;
import com.yas.system.catalog.internal.dto.request.ProductVariantAttributeValueUpdateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductVariantUpdateRequest;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.option.ProductOptionValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yas.system.common.constant.AppConstant.PRODUCT_VARIANT_DEFAULT_TITLE;
import static com.yas.system.common.util.StringUtils.isBlank;

@Component
public class ProductVariantHelper {

    public ProductVariant createVariant(ProductVariantCreateRequest request, Product product) {
        String title = isBlank(request.title()) ? PRODUCT_VARIANT_DEFAULT_TITLE : request.title();
        return ProductVariant.builder()
                .title(title)
                .sku(request.sku())
                .price(request.price())
                .quantity(request.quantity())
                .mediaId(request.mediaId())
                .product(product)
                .build();
    }

    public ProductVariant createVariant(ProductVariantUpdateRequest request, Product product) {
        String title = isBlank(request.title()) ? PRODUCT_VARIANT_DEFAULT_TITLE : request.title();
        return ProductVariant.builder()
                .title(title)
                .sku(request.sku())
                .price(request.price())
                .quantity(request.quantity())
                .mediaId(request.mediaId())
                .product(product)
                .build();
    }

    public void applyVariant(ProductVariantUpdateRequest request, ProductVariant variant) {
        String title = isBlank(request.title()) ? PRODUCT_VARIANT_DEFAULT_TITLE : request.title();
        variant.setTitle(title);
        variant.setSku(request.sku());
        variant.setPrice(request.price());
        variant.setQuantity(request.quantity());
        variant.setMediaId(request.mediaId());
    }

    public ProductVariantAttributeValue createVariantAttributeValue(
            ProductVariant variant,
            ProductAttribute attribute,
            String value
    ) {
        return ProductVariantAttributeValue.builder()
                .productVariant(variant)
                .productAttribute(attribute)
                .value(value)
                .build();
    }

    public VariantOptionValue createVariantOptionValue(
            ProductVariant productVariant,
            ProductOptionValue productOptionValue
    ) {
        return VariantOptionValue.builder()
                .productVariant(productVariant)
                .productOptionValue(productOptionValue)
                .build();
    }

    /**
     * Builds the {@link VariantUpdateContext} — a collection of pre-computed lookup maps —
     * used across all sub-operations of a variant delta-update.
     * Centralizing construction here keeps {@code ProductServiceImpl} free of data-preparation logic.
     */
    public VariantUpdateContext buildVariantUpdateContext(
            List<ProductOptionValue> productOptionValues,
            Map<String, ProductOptionValue> povByOptionAndValue,
            List<ProductVariant> currentVariants,
            List<VariantOptionValue> currentOptionValues,
            List<ProductVariantAttributeValue> currentVariantAttributes,
            Map<Long, ProductAttribute> productAttributeById
    ) {
        Map<Long, ProductOptionValue> povById = Objects.isNull(productOptionValues)
                ? Map.of()
                : productOptionValues.stream()
                        .filter(pov -> Objects.nonNull(pov.getId()))
                        .collect(Collectors.toMap(ProductOptionValue::getId, Function.identity(), (e1, e2) -> e1));

        Map<Long, ProductVariant> currentVariantById = Objects.isNull(currentVariants)
                ? Map.of()
                : currentVariants.stream()
                        .collect(Collectors.toMap(ProductVariant::getId, Function.identity()));

        Map<String, VariantOptionValue> existingOptionValueMap = Objects.isNull(currentOptionValues)
                ? Map.of()
                : currentOptionValues.stream()
                        .filter(vov -> Objects.nonNull(vov.getProductVariant())
                                && Objects.nonNull(vov.getProductOptionValue()))
                        .collect(Collectors.toMap(
                                vov -> vov.getProductVariant().getId() + "_" + vov.getProductOptionValue().getId(),
                                Function.identity(),
                                (e1, e2) -> e1
                        ));

        Map<String, ProductVariantAttributeValue> existingVariantAttrMap = Objects.isNull(currentVariantAttributes)
                ? Map.of()
                : currentVariantAttributes.stream()
                        .filter(vav -> Objects.nonNull(vav.getProductVariant())
                                && Objects.nonNull(vav.getProductAttribute()))
                        .collect(Collectors.toMap(
                                vav -> vav.getProductVariant().getId() + "_" + vav.getProductAttribute().getId(),
                                Function.identity(),
                                (e1, e2) -> e1
                        ));

        return new VariantUpdateContext(
                povById,
                povByOptionAndValue,
                currentVariantById,
                existingOptionValueMap,
                existingVariantAttrMap,
                productAttributeById
        );
    }
}
