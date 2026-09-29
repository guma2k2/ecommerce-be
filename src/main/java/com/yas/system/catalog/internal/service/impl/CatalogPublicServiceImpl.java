package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.api.CatalogPublicService;
import com.yas.system.catalog.api.dto.ProductVariantPublicDto;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.repository.ProductMediaRepository;
import com.yas.system.catalog.internal.repository.ProductVariantRepository;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.media.api.MediaPublicService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CatalogPublicServiceImpl implements CatalogPublicService {

    ProductVariantRepository productVariantRepository;
    ProductMediaRepository productMediaRepository;
    MediaPublicService mediaPublicService;

    @Override
    @Transactional(readOnly = true)
    public ProductVariantPublicDto getProductVariantById(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));

        String mediaId = variant.getMediaId();
        if (mediaId == null && variant.getProduct() != null) {
            List<ProductMedia> medias = productMediaRepository.findByProductIdOrderByPositionAsc(variant.getProduct().getId());
            if (!medias.isEmpty()) {
                mediaId = medias.getFirst().getMediaId();
            }
        }

        String thumbnailUrl = mediaId != null ? mediaPublicService.getMediaUrl(mediaId) : null;
        Product product = variant.getProduct();

        return new ProductVariantPublicDto(
                variant.getId(),
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                product != null ? product.getSlug() : null,
                thumbnailUrl,
                variant.getQuantity() != null ? variant.getQuantity() : 0,
                variant.getSku(),
                variant.getPrice(),
                variant.getStatus()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, ProductVariantPublicDto> getProductVariantsByIds(Collection<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return Map.of();
        }

        List<ProductVariant> variants = productVariantRepository.findAllById(variantIds);
        if (variants.isEmpty()) {
            return Map.of();
        }

        // Collect product IDs that might need fallback media
        List<Long> productIdsNeedingMedia = variants.stream()
                .filter(v -> v.getMediaId() == null && v.getProduct() != null)
                .map(v -> v.getProduct().getId())
                .distinct()
                .toList();

        Map<Long, String> productFirstMediaMap = new HashMap<>();
        if (!productIdsNeedingMedia.isEmpty()) {
            List<ProductMedia> productMedias = productMediaRepository.findByProductIdInOrderByPositionAsc(productIdsNeedingMedia);
            for (ProductMedia pm : productMedias) {
                if (pm.getProduct() != null) {
                    productFirstMediaMap.putIfAbsent(pm.getProduct().getId(), pm.getMediaId());
                }
            }
        }

        // Resolve mediaId for each variant
        Map<Long, String> variantMediaIdMap = new HashMap<>();
        Set<String> allMediaIds = new HashSet<>();

        for (ProductVariant variant : variants) {
            String mediaId = variant.getMediaId();
            if (mediaId == null && variant.getProduct() != null) {
                mediaId = productFirstMediaMap.get(variant.getProduct().getId());
            }
            if (mediaId != null) {
                variantMediaIdMap.put(variant.getId(), mediaId);
                allMediaIds.add(mediaId);
            }
        }

        Map<String, String> mediaUrls = allMediaIds.isEmpty()
                ? Map.of()
                : mediaPublicService.getMediaUrls(new ArrayList<>(allMediaIds));

        return variants.stream().collect(Collectors.toMap(
                ProductVariant::getId,
                variant -> {
                    Product product = variant.getProduct();
                    String mediaId = variantMediaIdMap.get(variant.getId());
                    String thumbnailUrl = mediaId != null ? mediaUrls.get(mediaId) : null;

                    return new ProductVariantPublicDto(
                            variant.getId(),
                            product != null ? product.getId() : null,
                            product != null ? product.getName() : null,
                            product != null ? product.getSlug() : null,
                            thumbnailUrl,
                            variant.getQuantity() != null ? variant.getQuantity() : 0,
                            variant.getSku(),
                            variant.getPrice(),
                            variant.getStatus()
                    );
                }
        ));
    }
}
