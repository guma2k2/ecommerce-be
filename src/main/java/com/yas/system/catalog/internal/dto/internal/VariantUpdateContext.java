package com.yas.system.catalog.internal.dto.internal;

import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductVariantAttributeValue;
import com.yas.system.catalog.internal.entity.option.ProductOptionValue;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;

import java.util.Map;

/**
 * Immutable context holding pre-built lookup maps needed during a product variant delta-update.
 * Built once before the update loop and passed into sub-handlers to avoid repeated construction.
 */
public record VariantUpdateContext(
        Map<Long, ProductOptionValue> povById,
        Map<String, ProductOptionValue> povByOptionAndValue,
        Map<Long, ProductVariant> currentVariantById,
        Map<String, VariantOptionValue> existingOptionValueMap,
        Map<String, ProductVariantAttributeValue> existingVariantAttrMap,
        Map<Long, ProductAttribute> productAttributeById
) {}
