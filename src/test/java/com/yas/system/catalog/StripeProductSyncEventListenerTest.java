package com.yas.system.catalog;

import com.yas.system.catalog.api.enumeration.ProductStatus;
import com.yas.system.catalog.events.ProductSyncEvent;
import com.yas.system.catalog.events.SyncAction;
import com.yas.system.catalog.events.VariantStripeInfo;
import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.listener.StripeProductSyncEventListener;
import com.yas.system.catalog.internal.repository.ProductMediaRepository;
import com.yas.system.catalog.internal.repository.ProductRepository;
import com.yas.system.catalog.internal.repository.ProductVariantRepository;
import com.yas.system.media.api.MediaPublicService;
import com.yas.system.payment.api.StripeCatalogPublicService;
import com.yas.system.payment.api.dto.StripeVariantSyncCommand;
import com.yas.system.payment.api.dto.StripeVariantSyncResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StripeProductSyncEventListener Tests")
class StripeProductSyncEventListenerTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductVariantRepository productVariantRepository;

    @Mock
    ProductMediaRepository productMediaRepository;

    @Mock
    MediaPublicService mediaPublicService;

    @Mock
    StripeCatalogPublicService stripeCatalogPublicService;

    StripeProductSyncEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new StripeProductSyncEventListener(
                productRepository,
                productVariantRepository,
                productMediaRepository,
                mediaPublicService,
                stripeCatalogPublicService
        );
    }

    @Test
    @DisplayName("DELETE action archives removed variants in Stripe")
    void handleDeleteSync_archivesVariants() {
        VariantStripeInfo info = new VariantStripeInfo(10L, "prod_del_1", "price_del_1");
        ProductSyncEvent event = new ProductSyncEvent(1L, SyncAction.DELETE, List.of(info));

        listener.handleStripeSync(event);

        verify(stripeCatalogPublicService).archiveVariant("prod_del_1", "price_del_1");
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("UPSERT action archives removed variants and syncs active variants to Stripe")
    void handleUpsertSync_archivesRemovedAndSyncsActive() {
        VariantStripeInfo removed = new VariantStripeInfo(99L, "prod_old", "price_old");
        ProductSyncEvent event = new ProductSyncEvent(1L, SyncAction.UPSERT, List.of(removed));

        Product product = Product.builder()
                .name("MacBook Pro")
                .description("Apple laptop")
                .status(ProductStatus.ACTIVE)
                .build();
        product.setId(1L);

        ProductVariant variant = ProductVariant.builder()
                .title("16-inch 1TB")
                .sku("MBP-16-1TB")
                .price(BigDecimal.valueOf(2499.00))
                .mediaId("media-1")
                .product(product)
                .build();
        variant.setId(101L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.findByProductId(1L)).thenReturn(List.of(variant));
        when(productMediaRepository.findByProductIdOrderByPositionAsc(1L)).thenReturn(List.of());
        when(mediaPublicService.getMediaUrls(any())).thenReturn(Map.of("media-1", "http://media.com/mbp.png"));
        when(stripeCatalogPublicService.syncVariant(any())).thenReturn(
                new StripeVariantSyncResult(101L, "prod_stripe_new", "price_stripe_new")
        );

        listener.handleStripeSync(event);

        verify(stripeCatalogPublicService).archiveVariant("prod_old", "price_old");

        ArgumentCaptor<StripeVariantSyncCommand> cmdCaptor = ArgumentCaptor.forClass(StripeVariantSyncCommand.class);
        verify(stripeCatalogPublicService).syncVariant(cmdCaptor.capture());
        StripeVariantSyncCommand capturedCmd = cmdCaptor.getValue();
        assertThat(capturedCmd.name()).isEqualTo("MacBook Pro - 16-inch 1TB");
        assertThat(capturedCmd.price()).isEqualByComparingTo(BigDecimal.valueOf(2499.00));
        assertThat(capturedCmd.imageUrl()).isEqualTo("http://media.com/mbp.png");
        assertThat(capturedCmd.active()).isTrue();

        assertThat(variant.getStripeProductId()).isEqualTo("prod_stripe_new");
        assertThat(variant.getStripePriceId()).isEqualTo("price_stripe_new");
        verify(productVariantRepository).saveAll(List.of(variant));
    }
}
