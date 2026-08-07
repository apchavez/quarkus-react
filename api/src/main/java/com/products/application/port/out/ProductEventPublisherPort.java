package com.products.application.port.out;

import com.products.domain.event.ProductEvent;

public interface ProductEventPublisherPort {

    void publish(ProductEvent event);
}
