package com.yas.system.payment.service;

import com.yas.system.payment.api.dto.StripeVariantSyncCommand;
import com.yas.system.payment.api.dto.StripeVariantSyncResult;

public interface StripeCatalogSyncService {

    StripeVariantSyncResult syncVariant(StripeVariantSyncCommand command);

    void archiveVariant(String stripeProductId, String stripePriceId);
}

