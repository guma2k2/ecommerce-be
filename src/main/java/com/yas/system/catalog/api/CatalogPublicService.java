package com.yas.system.catalog.api;

import com.yas.system.catalog.api.dto.ProductVariantPublicDto;

import java.util.Collection;
import java.util.Map;

/**
 * Public service interface exposed by the Catalog module for cross-module consumption,
 * adhering to Modular Monolith architecture rules.
 */
public interface CatalogPublicService {

    /**
     * Get product variant public details by variant ID.
     *
     * @param variantId Long ID of the variant
     * @return ProductVariantPublicDto
     */
    ProductVariantPublicDto getProductVariantById(Long variantId);

    /**
     * Bulk get product variant public details by variant IDs.
     *
     * @param variantIds Collection of variant IDs
     * @return Map of variantId -> ProductVariantPublicDto
     */
    Map<Long, ProductVariantPublicDto> getProductVariantsByIds(Collection<Long> variantIds);
}
