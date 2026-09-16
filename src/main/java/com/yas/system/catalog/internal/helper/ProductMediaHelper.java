package com.yas.system.catalog.internal.helper;

import com.yas.system.catalog.internal.dto.request.ProductMediaRequest;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yas.system.common.util.StringUtils.isBlank;

@Component
public class ProductMediaHelper {

    public List<ProductMedia> createProductMedias(List<ProductMediaRequest> mediaRequests, Product product) {
        if (Objects.isNull(mediaRequests) || mediaRequests.isEmpty()) {
            return List.of();
        }
        return mediaRequests.stream()
                .filter(req -> Objects.nonNull(req) && !isBlank(req.mediaId()))
                .map(req -> ProductMedia.builder()
                        .mediaId(req.mediaId())
                        .position(req.position())
                        .product(product)
                        .build())
                .toList();
    }

    public void calculateDeltaProductMedias(
            List<ProductMediaRequest> mediaRequests,
            Product product,
            List<ProductMedia> currentMedias,
            List<ProductMedia> outToSave,
            List<ProductMedia> outToDelete
    ) {
        if (Objects.isNull(mediaRequests) || mediaRequests.isEmpty()) {
            if (Objects.nonNull(currentMedias) && !currentMedias.isEmpty()) {
                outToDelete.addAll(currentMedias);
            }
            return;
        }

        Map<String, ProductMedia> currentMediaMap = Objects.isNull(currentMedias)
                ? Map.of()
                : currentMedias.stream().collect(Collectors.toMap(ProductMedia::getMediaId, Function.identity(), (e1, _) -> e1));

        Set<String> requestedMediaIds = new HashSet<>();

        for (ProductMediaRequest req : mediaRequests) {
            if (Objects.isNull(req) || isBlank(req.mediaId())) {
                continue;
            }
            requestedMediaIds.add(req.mediaId());
            ProductMedia existingMedia = currentMediaMap.get(req.mediaId());
            if (Objects.nonNull(existingMedia)) {
                existingMedia.setPosition(req.position());
                outToSave.add(existingMedia);
            } else {
                outToSave.add(ProductMedia.builder()
                        .mediaId(req.mediaId())
                        .position(req.position())
                        .product(product)
                        .build());
            }
        }

        if (Objects.nonNull(currentMedias)) {
            currentMedias.stream()
                    .filter(media -> !requestedMediaIds.contains(media.getMediaId()))
                    .forEach(outToDelete::add);
        }
    }

    public List<String> extractAllMediaIds(List<ProductMedia> medias, List<ProductVariant> variants) {
        List<String> mediaIds = new ArrayList<>();
        if (Objects.nonNull(medias)) {
            medias.stream()
                    .map(ProductMedia::getMediaId)
                    .filter(id -> Objects.nonNull(id) && !id.isBlank())
                    .forEach(mediaIds::add);
        }
        if (Objects.nonNull(variants)) {
            variants.stream()
                    .map(ProductVariant::getMediaId)
                    .filter(id -> Objects.nonNull(id) && !id.isBlank())
                    .forEach(mediaIds::add);
        }
        return mediaIds;
    }
}
