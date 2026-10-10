package com.yas.system.catalog.internal.listener;

import com.yas.system.catalog.events.ProductSyncEvent;
import com.yas.system.catalog.events.SyncAction;
import com.yas.system.catalog.events.VariantStripeInfo;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.repository.ProductMediaRepository;
import com.yas.system.catalog.internal.repository.ProductRepository;
import com.yas.system.catalog.internal.repository.ProductVariantRepository;
import com.yas.system.media.api.MediaPublicService;
import com.yas.system.common.util.StringUtils;
import com.yas.system.payment.api.StripeCatalogPublicService;
import com.yas.system.payment.api.dto.StripeVariantSyncCommand;
import com.yas.system.payment.api.dto.StripeVariantSyncResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripeProductSyncEventListener {

    ProductRepository productRepository;
    ProductVariantRepository productVariantRepository;
    ProductMediaRepository productMediaRepository;
    MediaPublicService mediaPublicService;
    StripeCatalogPublicService stripeCatalogPublicService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStripeSync(ProductSyncEvent event) {
        if (event == null) {
            log.debug("ProductSyncEvent is null, ignoring.");
            return;
        }

        int removedCount = event.removedVariants() != null ? event.removedVariants().size() : 0;
        log.info("Received Stripe ProductSyncEvent: productId={}, action={}, removedVariantsCount={}",
                event.productId(), event.action(), removedCount);

        if (event.action() == SyncAction.DELETE) {
            handleDeleteSync(event);
            return;
        }

        if (event.action() == SyncAction.UPSERT) {
            handleUpsertSync(event);
        }
    }

    private void handleDeleteSync(ProductSyncEvent event) {
        int count = event.removedVariants() != null ? event.removedVariants().size() : 0;
        log.info("Archiving {} variants in Stripe for deleted product ID: {}", count, event.productId());

        if (event.removedVariants() != null && !event.removedVariants().isEmpty()) {
            for (VariantStripeInfo info : event.removedVariants()) {
                log.debug("Archiving variant ID: {}, stripeProductId: {}, stripePriceId: {}",
                        info.variantId(), info.stripeProductId(), info.stripePriceId());
                stripeCatalogPublicService.archiveVariant(info.stripeProductId(), info.stripePriceId());
            }
        }
        log.info("Completed archiving variants in Stripe for deleted product ID: {}", event.productId());
    }

    private void handleUpsertSync(ProductSyncEvent event) {
        // Archive any variants omitted during update
        if (event.removedVariants() != null && !event.removedVariants().isEmpty()) {
            log.info("Archiving {} omitted variants from product ID: {}", event.removedVariants().size(), event.productId());
            for (VariantStripeInfo info : event.removedVariants()) {
                log.debug("Archiving omitted variant ID: {}, stripeProductId: {}, stripePriceId: {}",
                        info.variantId(), info.stripeProductId(), info.stripePriceId());
                stripeCatalogPublicService.archiveVariant(info.stripeProductId(), info.stripePriceId());
            }
        }

        Optional<Product> productOpt = productRepository.findById(event.productId());
        if (productOpt.isEmpty()) {
            log.warn("Product ID {} not found in database for Stripe sync. Skipping.", event.productId());
            return;
        }
        Product product = productOpt.get();

        List<ProductVariant> variants = productVariantRepository.findByProductId(event.productId());
        if (variants.isEmpty()) {
            log.info("No variants found for product ID: {}. Skipping Stripe sync.", event.productId());
            return;
        }

        log.info("Synchronizing {} variants to Stripe for product ID: {} ('{}')",
                variants.size(), product.getId(), product.getName());

        // Fetch media URLs
        List<ProductMedia> productMedias = productMediaRepository.findByProductIdOrderByPositionAsc(product.getId());
        String defaultMediaId = !productMedias.isEmpty() ? productMedias.getFirst().getMediaId() : null;
        log.debug("Default product media ID: {}", defaultMediaId);

        Set<String> mediaIdsToFetch = new HashSet<>();
        if (defaultMediaId != null) {
            mediaIdsToFetch.add(defaultMediaId);
        }
        for (ProductVariant v : variants) {
            if (v.getMediaId() != null) {
                mediaIdsToFetch.add(v.getMediaId());
            }
        }

        Map<String, String> mediaUrls = mediaIdsToFetch.isEmpty()
                ? Map.of()
                : mediaPublicService.getMediaUrls(new ArrayList<>(mediaIdsToFetch));
        log.debug("Resolved {} media URLs for product ID: {}", mediaUrls.size(), product.getId());

        List<ProductVariant> variantsToSave = new ArrayList<>();

        for (ProductVariant variant : variants) {
            String variantMediaId = variant.getMediaId() != null ? variant.getMediaId() : defaultMediaId;
            String imageUrl = variantMediaId != null ? mediaUrls.get(variantMediaId) : null;

            String name = product.getName();
            if (variant.getTitle() != null && !variant.getTitle().isBlank()) {
                name = name + " - " + variant.getTitle();
            }

            boolean active = product != null && product.isActive();

            log.debug("Preparing Stripe sync command for variant ID: {} (sku='{}', price={}, active={}, existingStripeProd='{}', existingStripePrice='{}')",
                    variant.getId(), variant.getSku(), variant.getPrice(), active, variant.getStripeProductId(), variant.getStripePriceId());

            String plainDescription = (product.getMetaDescription() != null && !product.getMetaDescription().isBlank())
                    ? product.getMetaDescription().trim()
                    : StringUtils.cleanHtmlToPlainText(product.getDescription(), 500);

            StripeVariantSyncCommand command = new StripeVariantSyncCommand(
                    product.getId(),
                    variant.getId(),
                    name,
                    plainDescription,
                    variant.getSku(),
                    variant.getPrice(),
                    "usd",
                    imageUrl,
                    variant.getStripeProductId(),
                    variant.getStripePriceId(),
                    active
            );

            StripeVariantSyncResult result = stripeCatalogPublicService.syncVariant(command);

            log.debug("Stripe sync result for variant ID {}: stripeProductId='{}', stripePriceId='{}'",
                    variant.getId(), result.stripeProductId(), result.stripePriceId());

            boolean updated = false;
            if (result.stripeProductId() != null && !Objects.equals(result.stripeProductId(), variant.getStripeProductId())) {
                variant.setStripeProductId(result.stripeProductId());
                updated = true;
            }
            if (result.stripePriceId() != null && !Objects.equals(result.stripePriceId(), variant.getStripePriceId())) {
                variant.setStripePriceId(result.stripePriceId());
                updated = true;
            }

            if (updated) {
                variantsToSave.add(variant);
            }
        }

        if (!variantsToSave.isEmpty()) {
            productVariantRepository.saveAll(variantsToSave);
            log.info("Successfully persisted Stripe IDs to DB for {}/{} variants of product ID: {}",
                    variantsToSave.size(), variants.size(), product.getId());
        } else {
            log.debug("No variant Stripe IDs required DB update for product ID: {}", product.getId());
        }
    }
}
