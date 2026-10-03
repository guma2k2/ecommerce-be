package com.yas.system.payment.api;

import com.yas.system.payment.api.dto.StripeVariantSyncCommand;
import com.yas.system.payment.api.dto.StripeVariantSyncResult;

public interface StripeCatalogPublicService {

    StripeVariantSyncResult syncVariant(StripeVariantSyncCommand command);

    void archiveVariant(String stripeProductId, String stripePriceId);
}
