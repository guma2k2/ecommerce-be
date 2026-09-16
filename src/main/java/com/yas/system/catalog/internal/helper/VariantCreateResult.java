package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;

import java.util.List;

/**
 * Immutable result returned from a product variant creation pass.
 * Replaces mutable out-parameter lists in {@code saveVariants}.
 */
public record VariantCreateResult(
        List<ProductVariant> savedVariants,
        List<VariantOptionValue> savedOptionValues,
        List<ProductVariantAttributeValue> savedVariantAttributeValues
) {}
