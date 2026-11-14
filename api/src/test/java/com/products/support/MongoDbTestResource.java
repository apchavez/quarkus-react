package com.products.support;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class MongoDbTestResource implements QuarkusTestResourceLifecycleManager {

    private static final MongoDBContainer MONGO =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @Override
    public Map<String, String> start() {
        MONGO.start();
        return Map.of(
                "quarkus.mongodb.connection-string", MONGO.getConnectionString(),
                "quarkus.mongodb.database", "products_test"
        );
    }

    @Override
    public void stop() {
        if (MONGO.isRunning()) {
            MONGO.stop();
        }
    }
}
