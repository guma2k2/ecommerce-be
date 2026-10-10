package com.yas.system.payment.internal.service.impl;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceUpdateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductUpdateParams;
import com.yas.system.payment.api.StripeCatalogPublicService;
import com.yas.system.payment.api.dto.StripeVariantSyncCommand;
import com.yas.system.payment.api.dto.StripeVariantSyncResult;
import com.yas.system.common.util.StringUtils;
import com.yas.system.payment.internal.config.StripeProperties;
import com.yas.system.payment.internal.constant.PaymentConstant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripeCatalogPublicServiceImpl implements StripeCatalogPublicService {

    StripeProperties stripeProperties;

    @Override
    public StripeVariantSyncResult syncVariant(StripeVariantSyncCommand command) {
        if (!stripeProperties.isSyncEnabled()) {
            log.debug("Stripe catalog sync is disabled or apiKey is missing. Skipping variant ID: {}", command.variantId());
            return new StripeVariantSyncResult(command.variantId(), command.existingStripeProductId(), command.existingStripePriceId());
        }

        String currency = (command.currency() != null && !command.currency().isBlank())
                ? command.currency().toLowerCase()
                : PaymentConstant.DEFAULT_CURRENCY;

        long unitAmountInCents = command.price() != null
                ? command.price().multiply(PaymentConstant.CENTS_MULTIPLIER).longValue()
                : 0L;

        log.debug("Starting Stripe sync for variant ID: {} (name='{}', price={}, unitAmountInCents={}, currency='{}', existingProd='{}', existingPrice='{}')",
                command.variantId(), command.name(), command.price(), unitAmountInCents, currency, command.existingStripeProductId(), command.existingStripePriceId());

        StripeClient client = new StripeClient(stripeProperties.apiKey());

        try {
            if (command.existingStripeProductId() == null || command.existingStripeProductId().isBlank()) {
                // CREATE new product and price in Stripe
                return createStripeProductAndPrice(client, command, currency, unitAmountInCents);
            } else {
                // UPDATE existing product and price in Stripe
                return updateStripeProductAndPrice(client, command, currency, unitAmountInCents);
            }
        } catch (StripeException e) {
            log.error("Failed to sync variant {} with Stripe: {}", command.variantId(), e.getMessage(), e);
            // Return existing ids on failure so database isn't corrupted
            return new StripeVariantSyncResult(command.variantId(), command.existingStripeProductId(), command.existingStripePriceId());
        }
    }

    private StripeVariantSyncResult createStripeProductAndPrice(
            StripeClient client,
            StripeVariantSyncCommand command,
            String currency,
            long unitAmountInCents
    ) throws StripeException {
        log.debug("Sending Stripe product.create for variant ID: {} (name='{}', amount={}, currency='{}')",
                command.variantId(), command.name(), unitAmountInCents, currency);

        ProductCreateParams.DefaultPriceData defaultPriceData = ProductCreateParams.DefaultPriceData.builder()
                .setCurrency(currency)
                .setUnitAmount(unitAmountInCents)
                .build();

        ProductCreateParams.Builder paramsBuilder = ProductCreateParams.builder()
                .setName(command.name())
                .setDefaultPriceData(defaultPriceData)
                .setActive(command.active())
                .putMetadata(PaymentConstant.MetadataKey.PRODUCT_ID, String.valueOf(command.productId()))
                .putMetadata(PaymentConstant.MetadataKey.VARIANT_ID, String.valueOf(command.variantId()))
                .putMetadata(PaymentConstant.MetadataKey.SKU, command.sku() != null ? command.sku() : "");

        if (command.description() != null && !command.description().isBlank()) {
            String cleanDesc = StringUtils.cleanHtmlToPlainText(command.description(), PaymentConstant.STRIPE_MAX_DESCRIPTION_LENGTH);
            if (!cleanDesc.isBlank()) {
                paramsBuilder.setDescription(cleanDesc);
            }
        }

        if (command.imageUrl() != null && !command.imageUrl().isBlank()) {
            paramsBuilder.addImage(command.imageUrl());
        }

        Product stripeProduct = client.products().create(paramsBuilder.build());
        String priceId = stripeProduct.getDefaultPrice();

        log.info("Successfully created Stripe Product {} and Default Price {} for variant ID: {}",
                stripeProduct.getId(), priceId, command.variantId());

        return new StripeVariantSyncResult(command.variantId(), stripeProduct.getId(), priceId);
    }

    private StripeVariantSyncResult updateStripeProductAndPrice(
            StripeClient client,
            StripeVariantSyncCommand command,
            String currency,
            long unitAmountInCents
    ) throws StripeException {
        String stripeProductId = command.existingStripeProductId();
        String currentPriceId = command.existingStripePriceId();

        log.debug("Updating existing Stripe Product {} for variant ID: {}", stripeProductId, command.variantId());

        boolean needNewPrice = false;
        if (currentPriceId != null && !currentPriceId.isBlank()) {
            try {
                Price existingPrice = client.prices().retrieve(currentPriceId);
                log.debug("Retrieved existing Stripe price {}: currentAmount={}, requestedAmount={}, currentCurrency={}, requestedCurrency={}",
                        currentPriceId, existingPrice.getUnitAmount(), unitAmountInCents, existingPrice.getCurrency(), currency);

                if (existingPrice.getUnitAmount() == null
                        || existingPrice.getUnitAmount() != unitAmountInCents
                        || !currency.equalsIgnoreCase(existingPrice.getCurrency())) {
                    needNewPrice = true;
                    log.debug("Price change detected for variant ID: {}. New Price required.", command.variantId());
                }
            } catch (StripeException e) {
                log.warn("Could not retrieve existing Stripe price {}: {}. Creating replacement price.", currentPriceId, e.getMessage());
                needNewPrice = true;
            }
        } else {
            log.debug("No existing Stripe price ID found on variant ID: {}. New Price required.", command.variantId());
            needNewPrice = true;
        }

        if (needNewPrice) {
            log.info("Creating new Stripe price for product {} with amount: {} {}", stripeProductId, unitAmountInCents, currency);
            PriceCreateParams priceParams = PriceCreateParams.builder()
                    .setProduct(stripeProductId)
                    .setCurrency(currency)
                    .setUnitAmount(unitAmountInCents)
                    .setActive(true)
                    .build();

            Price newPrice = client.prices().create(priceParams);
            String oldPriceId = currentPriceId;
            currentPriceId = newPrice.getId();

            ProductUpdateParams updateParams = ProductUpdateParams.builder()
                    .setDefaultPrice(currentPriceId)
                    .build();
            client.products().update(stripeProductId, updateParams);
            log.debug("Assigned new default price {} to product {}", currentPriceId, stripeProductId);

            if (oldPriceId != null && !oldPriceId.isBlank()) {
                try {
                    client.prices().update(oldPriceId, PriceUpdateParams.builder().setActive(false).build());
                    log.debug("Deactivated previous Stripe price {}", oldPriceId);
                } catch (StripeException e) {
                    log.warn("Could not deactivate previous Stripe price {}: {}", oldPriceId, e.getMessage());
                }
            }
        }

        ProductUpdateParams.Builder productUpdateBuilder = ProductUpdateParams.builder()
                .setName(command.name())
                .setActive(command.active())
                .putMetadata(PaymentConstant.MetadataKey.PRODUCT_ID, String.valueOf(command.productId()))
                .putMetadata(PaymentConstant.MetadataKey.VARIANT_ID, String.valueOf(command.variantId()))
                .putMetadata(PaymentConstant.MetadataKey.SKU, command.sku() != null ? command.sku() : "");

        if (command.description() != null && !command.description().isBlank()) {
            String cleanDesc = StringUtils.cleanHtmlToPlainText(command.description(), PaymentConstant.STRIPE_MAX_DESCRIPTION_LENGTH);
            if (!cleanDesc.isBlank()) {
                productUpdateBuilder.setDescription(cleanDesc);
            }
        }

        if (command.imageUrl() != null && !command.imageUrl().isBlank()) {
            productUpdateBuilder.addImage(command.imageUrl());
        }

        client.products().update(stripeProductId, productUpdateBuilder.build());

        log.info("Successfully updated Stripe Product {} with active Price {} for variant ID: {}",
                stripeProductId, currentPriceId, command.variantId());

        return new StripeVariantSyncResult(command.variantId(), stripeProductId, currentPriceId);
    }

    @Override
    public void archiveVariant(String stripeProductId, String stripePriceId) {
        if (!stripeProperties.isSyncEnabled()) {
            log.debug("Stripe sync disabled. Skipping archive for prod={}, price={}", stripeProductId, stripePriceId);
            return;
        }

        log.info("Archiving Stripe product: '{}', price: '{}'", stripeProductId, stripePriceId);
        StripeClient client = new StripeClient(stripeProperties.apiKey());

        if (stripeProductId != null && !stripeProductId.isBlank()) {
            try {
                client.products().update(stripeProductId, ProductUpdateParams.builder().setActive(false).build());
                log.info("Archived Stripe product: {}", stripeProductId);
            } catch (StripeException e) {
                log.warn("Failed to archive Stripe product {}: {}", stripeProductId, e.getMessage());
            }
        }

        if (stripePriceId != null && !stripePriceId.isBlank()) {
            try {
                client.prices().update(stripePriceId, PriceUpdateParams.builder().setActive(false).build());
                log.info("Archived Stripe price: {}", stripePriceId);
            } catch (StripeException e) {
                log.warn("Failed to archive Stripe price {}: {}", stripePriceId, e.getMessage());
            }
        }
    }
}
