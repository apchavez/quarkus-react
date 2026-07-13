package com.products.adapters.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.products.domain.event.ProductEvent;
import com.products.domain.event.ProductEventType;
import com.products.domain.model.Product;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaProductEventPublisherTest {

    private KafkaProductEventPublisher publisher;
    private Emitter<String> emitter;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        publisher = new KafkaProductEventPublisher();
        emitter = mock(Emitter.class);
        publisher.emitter = emitter;
        publisher.objectMapper = new ObjectMapper();
    }

    private Product product() {
        Product product = new Product();
        product.id = new ObjectId();
        product.sku = "SKU-1";
        return product;
    }

    @Test
    void publish_shouldSendSerializedEventToEmitter() {
        when(emitter.send(any(String.class))).thenReturn(CompletableFuture.completedFuture(null));

        publisher.publish(ProductEvent.of(ProductEventType.PRODUCT_CREATED, product()));

        verify(emitter).send(any(String.class));
    }

    @Test
    void publish_shouldSwallowFailure_whenEmitterThrowsSynchronously() {
        when(emitter.send(any(String.class))).thenThrow(new RuntimeException("kafka down"));

        assertThatCode(() -> publisher.publish(ProductEvent.of(ProductEventType.PRODUCT_UPDATED, product())))
                .doesNotThrowAnyException();
    }

    @Test
    void publish_shouldSwallowFailure_whenCompletionStageFailsAsynchronously() {
        CompletableFuture<Void> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker unreachable"));
        when(emitter.send(any(String.class))).thenReturn(failed);

        assertThatCode(() -> publisher.publish(ProductEvent.of(ProductEventType.PRODUCT_DELETED, product())))
                .doesNotThrowAnyException();
    }
}
