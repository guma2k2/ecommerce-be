package com.yas.system.payment;

import com.yas.system.payment.api.dto.StripeVariantSyncCommand;
import com.yas.system.payment.api.dto.StripeVariantSyncResult;
import com.yas.system.payment.internal.config.StripeProperties;
import com.yas.system.payment.internal.service.impl.StripeCatalogPublicServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StripeCatalogPublicService Tests")
class StripeCatalogPublicServiceTest {

    @Test
    @DisplayName("syncVariant returns existing IDs when sync is disabled or apiKey is missing")
    void syncVariant_disabled_returnsExistingIds() {
        StripeProperties properties = new StripeProperties(
                null,
                "whsec_test",
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                false
        );
        StripeCatalogPublicServiceImpl service = new StripeCatalogPublicServiceImpl(properties);

        StripeVariantSyncCommand command = new StripeVariantSyncCommand(
                1L,
                10L,
                "iPhone 15 - Black",
                "Description",
                "SKU-100",
                BigDecimal.valueOf(999.00),
                "USD",
                "http://media/img.png",
                "prod_existing",
                "price_existing",
                true
        );

        StripeVariantSyncResult result = service.syncVariant(command);

        assertThat(result.variantId()).isEqualTo(10L);
        assertThat(result.stripeProductId()).isEqualTo("prod_existing");
        assertThat(result.stripePriceId()).isEqualTo("price_existing");
    }

    @Test
    @DisplayName("archiveVariant does nothing when sync is disabled")
    void archiveVariant_disabled_doesNotThrow() {
        StripeProperties properties = new StripeProperties(
                "",
                "whsec_test",
                "http://localhost:3000/success",
                "http://localhost:3000/cancel",
                false
        );
        StripeCatalogPublicServiceImpl service = new StripeCatalogPublicServiceImpl(properties);

        service.archiveVariant("prod_123", "price_123");
    }
}
