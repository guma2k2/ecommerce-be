package com.yas.system.catalog.internal.dto.internal;

import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;

import java.util.List;
import java.util.Set;

/**
 * Immutable result returned from a variant delta-update pass.
 * Replaces mutable out-parameter lists and avoids side-effect-based data passing.
 */
public record VariantUpdateResult(
        List<ProductVariant> savedVariants,
        List<VariantOptionValue> savedOptionValues,
        List<ProductVariantAttributeValue> savedVariantAttributeValues,
        Set<Long> processedOptionValueIds,
        Set<Long> processedAttributeValueIds
) {}
