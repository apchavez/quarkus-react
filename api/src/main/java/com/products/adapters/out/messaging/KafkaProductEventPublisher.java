package com.products.adapters.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.application.port.out.ProductEventPublisherPort;
import com.products.domain.event.ProductEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class KafkaProductEventPublisher implements ProductEventPublisherPort {

    private static final Logger log = Logger.getLogger(KafkaProductEventPublisher.class);

    @Inject
    @Channel("product-events")
    Emitter<String> emitter;

    @Inject
    ObjectMapper objectMapper;

    // Fire-and-forget: a Kafka outage must never fail the calling create/update/delete operation.
    @Override
    public void publish(ProductEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            emitter.send(json).whenComplete((success, failure) -> {
                if (failure != null) {
                    log.warn("Failed to publish event: type=" + event.eventType(), failure);
                } else {
                    log.info("Event published: type=" + event.eventType() + ", productId="
                            + event.product().id);
                }
            });
        } catch (Exception e) {
            log.warn("Failed to publish event: type=" + event.eventType(), e);
        }
    }
}
