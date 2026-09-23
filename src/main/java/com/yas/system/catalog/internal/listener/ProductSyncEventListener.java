package com.yas.system.catalog.internal.listener;

import com.yas.system.catalog.events.ProductSyncEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductSyncEventListener {

    @Async
    @EventListener
    public void handleProductSync(ProductSyncEvent event) {
        log.info("Received ProductSyncEvent for product ID: {}, action: {}. Ready for Elasticsearch index sync.",
                event.productId(), event.action());
        // In PostgreSQL mode, data is already persisted directly in the relational schema.
        // When Elasticsearch is adopted, this handler delegates to Elasticsearch indexer.
    }
}
